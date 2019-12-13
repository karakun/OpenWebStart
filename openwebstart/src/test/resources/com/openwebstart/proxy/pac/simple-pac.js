function FindProxyForURL(url, host) {
    if(host === 'myserver') {
        return "DIRECT";
    }
    if(host === 'noproxy') {
        return null;
    }
    return "PROXY proxy.example.com:8080; DIRECT";
}