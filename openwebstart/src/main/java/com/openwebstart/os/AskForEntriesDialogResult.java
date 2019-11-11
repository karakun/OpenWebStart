package com.openwebstart.os;

public class AskForEntriesDialogResult {

    private final boolean desktopSelected;

    private final boolean menuSelected;

    public AskForEntriesDialogResult(final boolean desktopSelected, final boolean menuSelected) {
        this.desktopSelected = desktopSelected;
        this.menuSelected = menuSelected;
    }

    public boolean isDesktopSelected() {
        return desktopSelected;
    }

    public boolean isMenuSelected() {
        return menuSelected;
    }

}
