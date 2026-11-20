package com.example.btlquanlynhatro;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NavigationHelper {

    public static void setupNav(Activity activity, int currentTabId) {
        LinearLayout menuOverview = activity.findViewById(R.id.menuOverview);
        LinearLayout menuRoom = activity.findViewById(R.id.menuRoom);
        LinearLayout menuBill = activity.findViewById(R.id.menuBill);
        LinearLayout menuStats = activity.findViewById(R.id.menuStats);
        LinearLayout menuProfile = activity.findViewById(R.id.menuProfile);

        // Highlight tab hiện tại
        if (currentTabId == 1 && menuOverview != null) {
            ((TextView) activity.findViewById(R.id.txtOverview)).setTextColor(0xFF3498DB);
        } else if (currentTabId == 2 && menuRoom != null) {
            ((TextView) activity.findViewById(R.id.txtRoom)).setTextColor(0xFF3498DB);
        } else if (currentTabId == 3 && menuBill != null) {
            ((TextView) activity.findViewById(R.id.txtBill)).setTextColor(0xFF3498DB);
        } else if (currentTabId == 4 && menuStats != null) {
            ((TextView) activity.findViewById(R.id.txtStats)).setTextColor(0xFF3498DB);
        } else if (currentTabId == 5 && menuProfile != null) {
            ((TextView) activity.findViewById(R.id.txtProfile)).setTextColor(0xFF3498DB);
        }

        // Bắt sự kiện chuyển màn hình
        if (menuOverview != null && currentTabId != 1) {
            menuOverview.setOnClickListener(v -> {
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
                activity.overridePendingTransition(0, 0);
            });
        }
        if (menuRoom != null && currentTabId != 2) {
            menuRoom.setOnClickListener(v -> {
                 activity.startActivity(new Intent(activity, NhaTroActivity.class));
                 activity.finish();
                 activity.overridePendingTransition(0, 0);
            });
        }
        if (menuBill != null && currentTabId != 3) {
            menuBill.setOnClickListener(v -> {
                 activity.startActivity(new Intent(activity, HoaDonActivity.class));
                 activity.finish();
                 activity.overridePendingTransition(0, 0);
            });
        }
        if (menuStats != null && currentTabId != 4) {
            menuStats.setOnClickListener(v -> {
                 activity.startActivity(new Intent(activity, ThongKeActivity.class));
                 activity.finish();
                 activity.overridePendingTransition(0, 0);
            });
        }
        if (menuProfile != null && currentTabId != 5) {
            menuProfile.setOnClickListener(v -> {
                 activity.startActivity(new Intent(activity, ProfileActivity.class));
                 activity.finish();
                 activity.overridePendingTransition(0, 0);
            });
        }
    }
}