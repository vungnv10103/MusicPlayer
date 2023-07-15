package com.envy.playermusic.utils;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

@SuppressLint("RestrictedApi")
public class Badge {
    public void badge(@NonNull BottomNavigationView bottomNavigationView, int itemID, boolean visible, int number) {
        MenuBuilder menuBuilder = (MenuBuilder) bottomNavigationView.getMenu();
        MenuItemImpl menuItem = (MenuItemImpl) menuBuilder.findItem(itemID);
        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(menuItem.getItemId());
        badge.setNumber(number);
        badge.setVisible(visible);
    }
}
