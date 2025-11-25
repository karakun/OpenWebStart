#!/usr/bin/env python3
from __future__ import annotations

import argparse
import dataclasses
import json
from typing import Dict, List, Optional, Tuple
import requests

TIMEOUT = 25
HEADERS = {"User-Agent": "jvms-json-updater/1.4 (+https://karakun.com)"}

FEATURE_VERSIONS = [8, 11, 17, 21]

OS_MATRIX = [
    ("LINUX64",   {"vendor_os": "linux", "arch": "x64",    "bitness": 64}),
    ("LINUX32",   {"vendor_os": "linux", "arch": "x86",    "bitness": 32}),
    ("MAC64",     {"vendor_os": "mac",   "arch": "x64",    "bitness": 64}),
    ("WIN64",     {"vendor_os": "windows","arch": "x64",   "bitness": 64}),
    ("WIN32",     {"vendor_os": "windows","arch": "x86",   "bitness": 32}),
]
OS_MATRIX_ARM64 = [
    ("MACARM64",  {"vendor_os": "mac",   "arch": "aarch64","bitness": 64}),
]

PREFERRED_EXT = {
    "WIN": "zip",
    "LINUX": "tar.gz",
    "MAC": "tar.gz",
}

def _dbg(verbose: bool, *args):
    if verbose:
        try:
            print(*args)
        except Exception:
            pass

def http_head_ok(url: str, verbose: bool=False) -> bool:
    try:
        r = requests.head(url, timeout=TIMEOUT, allow_redirects=True, headers=HEADERS)
        if 200 <= r.status_code < 400:
            return True
        if r.status_code in (403, 405):
            g = requests.get(url, timeout=TIMEOUT, allow_redirects=True, headers=HEADERS, stream=True)
            try:
                if 200 <= g.status_code < 400:
                    return True
            finally:
                try:
                    g.close()
                except Exception:
                    pass
        if verbose:
            print("HEAD/GET fail", r.status_code, url)
    except requests.RequestException as e:
        if verbose:
            print("HEAD error", e, url)
        return False
    return False

def pick_format_for(os_label: str) -> str:
    if os_label.startswith("WIN"): return PREFERRED_EXT["WIN"]
    if os_label.startswith("LINUX"): return PREFERRED_EXT["LINUX"]
    return PREFERRED_EXT["MAC"]

def normalize_version(ver):
    if ver is None:
        return None
    if isinstance(ver, (list, tuple)):
        return ".".join(str(x) for x in ver)
    s = str(ver).strip()
    if "+" in s:
        s = s.split("+", 1)[0]
    low = s.lower()
    def _read_digits_left(text, i):
        j = i
        while j >= 0 and text[j].isdigit():
            j -= 1
        return text[j+1:i+1]
    def _read_digits_right(text, i):
        j = i
        n = len(text)
        while j < n and text[j].isdigit():
            j += 1
        return text[i:j]
    for idx, ch in enumerate(low):
        if ch == "u" and idx + 1 < len(low) and low[idx + 1].isdigit():
            left = _read_digits_left(low, idx - 1) if idx - 1 >= 0 else ""
            right = _read_digits_right(low, idx + 1)
            if left and right:
                return f"{int(left)}.0.{int(right)}"
    if low.startswith("1."):
        nums = []
        cur = ""
        for ch in s:
            if ch.isdigit():
                cur += ch
            else:
                if cur:
                    nums.append(int(cur))
                    cur = ""
        if cur:
            nums.append(int(cur))
        if len(nums) >= 2:
            major = nums[1]
            update = nums[-1]
            return f"{major}.0.{update}"
    return s

def adoptium_latest(feature: int, vendor_os: str, arch: str, bitness: int, want_ext: str, verbose: bool=False) -> Optional[Dict]:
    os_param = {"mac": "mac", "linux": "linux", "windows": "windows"}.get(vendor_os, vendor_os)
    params = {"os": os_param, "architecture": arch, "image_type": "jdk", "release_type": "ga"}
    url = f"https://api.adoptium.net/v3/assets/latest/{feature}/hotspot"
    try:
        resp = requests.get(url, params=params, timeout=TIMEOUT, headers=HEADERS)
        _dbg(verbose, f"[Temurin] feature={feature} os={vendor_os} arch={arch} -> {resp.status_code}")
        resp.raise_for_status()
        assets = resp.json()
        _dbg(verbose, f"[Temurin] items={len(assets) if isinstance(assets,list) else 'n/a'}")
        if not isinstance(assets, list) or not assets:
            _dbg(verbose, "[Temurin] empty result"); return None
        exts_any = (".zip", ".tar.gz", ".tgz")
        prefer = "." + want_ext if not want_ext.startswith(".") else want_ext
        candidates = []
        for item in assets:
            pkg = item.get("package", {}) or item.get("binary", {}).get("package", {})
            name = pkg.get("name", ""); link = pkg.get("link", "")
            vobj = item.get("version", {}) or item.get("version_data", {})
            ver  = normalize_version(vobj.get("semver") or vobj.get("openjdk_version"))
            if link: candidates.append((name, link, ver))
        for name, link, ver in candidates:
            if (name.endswith(prefer) or link.endswith(prefer)) and http_head_ok(link, verbose):
                return {"version": ver, "href": link}
        for name, link, ver in candidates:
            if (name.endswith(exts_any) or link.endswith(exts_any)) and http_head_ok(link, verbose):
                return {"version": ver, "href": link}
    except requests.RequestException:
        return None
    return None

def azul_latest(feature: int, vendor_os: str, arch: str, bitness: int, want_ext: str, verbose: bool=False) -> Optional[Dict]:
    url = "https://api.azul.com/metadata/v1/zulu/packages"
    os_map  = {"linux": "linux", "windows": "windows", "mac": "macos"}
    arch_map = {"x64": "x86", "x86": "x86", "aarch64": "aarch64"}
    exts_any = (".zip", ".tar.gz", ".tgz")
    prefer = "." + want_ext if not want_ext.startswith(".") else want_ext
    def choose(bundles):
        for b in bundles:
            link = b.get("download_url") or b.get("downloadUrl")
            fn = b.get("bundle_file_name") or b.get("filename") or ""
            ver = normalize_version(b.get("java_version") or b.get("version") or b.get("semver"))
            if not link: continue
            if (fn.endswith(prefer) or link.endswith(prefer)) and http_head_ok(link, verbose):
                return {"version": ver, "href": link}
        for b in bundles:
            link = b.get("download_url") or b.get("downloadUrl")
            fn = b.get("bundle_file_name") or b.get("filename") or ""
            ver = normalize_version(b.get("java_version") or b.get("version") or b.get("semver"))
            if not link: continue
            if (fn.endswith(exts_any) or link.endswith(exts_any)) and http_head_ok(link, verbose):
                return {"version": ver, "href": link}
        return None
    base_params = {
        "java-version": feature,
        "os": os_map.get(vendor_os, vendor_os),
        "arch": arch_map.get(arch, arch),
        "hw_bitness": bitness,
        "bundle_type": "jdk",
        "release_status": "ga",
        "availability_types": "ca",
        "latest": "true",
    }
    for attempt, params in enumerate([base_params, {k: v for k, v in base_params.items() if k != "availability_types"}], start=1):
        try:
            resp = requests.get(url, params=params, timeout=TIMEOUT, headers=HEADERS)
            _dbg(verbose, f"[Azul] attempt={attempt} feature={feature} os={params['os']} arch={params['arch']} {bitness}-bit -> {resp.status_code} URL={resp.url}")
            resp.raise_for_status()
            bundles = resp.json()
            _dbg(verbose, f"[Azul] items={len(bundles) if isinstance(bundles, list) else 'n/a'}")
            if not isinstance(bundles, list) or not bundles: continue
            picked = choose(bundles)
            if picked: return picked
        except requests.RequestException as e:
            _dbg(verbose, "[Azul] error", e); continue
    _dbg(verbose, "[Azul] no match"); return None

def bellsoft_latest(feature: int, vendor_os: str, arch: str, bitness: int, want_ext: str, verbose: bool=False) -> Optional[Dict]:
    os_map = {"linux": "linux", "mac": "macos", "macos": "macos", "windows": "windows"}
    os_val = os_map.get(vendor_os, vendor_os)
    a = arch.lower()
    if a in ("x64", "amd64", "x86_64"):
        arch_val = "x86"; bitness_val = 64
    elif a in ("x86", "i386", "i686"):
        arch_val = "x86"; bitness_val = 32 if bitness == 32 else 64
    elif a in ("aarch64", "arm64", "arm"):
        arch_val = "arm"; bitness_val = 64
    else:
        return None
    package_type = "zip" if os_val in ("macos", "windows") else "tar.gz"
    def _query(params) -> Optional[Dict]:
        try:
            url = "https://api.bell-sw.com/v1/liberica/releases"
            resp = requests.get(url, params=params, timeout=TIMEOUT, headers=HEADERS)
            _dbg(verbose, f"[BellSoft] params={params} -> {resp.status_code} URL={resp.url}")
            resp.raise_for_status()
            items = resp.json()
            if not isinstance(items, list) or not items: return None
            preferred_ext = (".zip", ".tar.gz", ".tgz")
            for it in items:
                link = it.get("downloadUrl") or it.get("download_url")
                fn = (it.get("filename") or "")
                if not link: continue
                if (fn.endswith(package_type) or link.endswith(package_type)) or fn.endswith(preferred_ext) or link.endswith(preferred_ext):
                    if http_head_ok(link, verbose):
                        ver = normalize_version(it.get("version") or it.get("semver"))
                        return {"version": ver, "href": link}
            return None
        except requests.RequestException as e:
            _dbg(verbose, "[BellSoft] error", e); return None
    params_primary = {
        "version-modifier": "latest",
        "version-feature": str(feature),
        "os": os_val,
        "arch": arch_val,
        "bitness": str(bitness_val),
        "bundle-type": "jdk",
        "package-type": package_type,
    }
    params_no_pkg = {k: v for k, v in params_primary.items() if k != "package-type"}
    params_latest_any = {k: v for k, v in params_primary.items() if k not in ("version-feature",)}
    for p in (params_primary, params_no_pkg, params_latest_any):
        picked = _query(p)
        if picked: return picked
    _dbg(verbose, "[BellSoft] no match"); return None

@dataclasses.dataclass
class Entry:
    version: str
    vendor: str
    os: str
    href: str

def make_entry(vendor: str, os_label: str, version: str, href: str) -> Entry:
    print("Added entry:", vendor, os_label, version)
    return Entry(version=version, vendor=vendor, os=os_label, href=href)

def collect_vendor_entries(vendor_name: str, resolver, feature: int, os_label: str, meta: Dict, verbose: bool=False) -> Optional[Entry]:
    want_ext = pick_format_for(os_label)
    result = resolver(feature, meta["vendor_os"], meta["arch"], meta.get("bitness", 64), want_ext, verbose)
    if result and result.get("href") and result.get("version"):
        return make_entry(vendor_name, os_label, result["version"], result["href"])
    return None

def generate(verbose: bool=False) -> Dict[str, List[Dict]]:
    runtimes: List[Dict] = []
    runtimes_arm: List[Dict] = []
    vendors = [("Eclipse Temurin", adoptium_latest), ("Zulu Community Edition", azul_latest), ("BellSoft Liberica", bellsoft_latest)]
    for vendor_name, resolver in vendors:
        for feature in FEATURE_VERSIONS:
            for os_label, meta in OS_MATRIX:
                entry = collect_vendor_entries(vendor_name, resolver, feature, os_label, meta, verbose)
                if entry: runtimes.append(dataclasses.asdict(entry))
    for vendor_name, resolver in vendors:
        for feature in FEATURE_VERSIONS:
            for os_label, meta in OS_MATRIX_ARM64:
                entry = collect_vendor_entries(vendor_name, resolver, feature, os_label, meta, verbose)
                if entry: runtimes_arm.append(dataclasses.asdict(entry))
    return {"runtimes": runtimes, "runtimes_1.6": runtimes_arm}

def main() -> int:
    ap = argparse.ArgumentParser(description="Generate jvms.json with latest versions and working links.")
    ap.add_argument("--out", default="jvms.json")
    ap.add_argument("--verbose", action="store_true", help="Print debug info (HTTP failures, fallbacks)")
    args = ap.parse_args()
    data = generate(verbose=args.verbose)
    with open(args.out, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    total_main=len(data['runtimes']); total_arm=len(data['runtimes_1.6'])
    print(f"Wrote {args.out} with {total_main} runtimes and {total_arm} macARM64 runtimes.")
    if total_main==0 and total_arm==0:
        print("No entries generated. Try --verbose to debug, check your network, or vendor API availability.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())

