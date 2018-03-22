package xyz.teamcatalyst.breedr.notifications;

import java.util.Comparator;

class SortByDate implements Comparator<NotificationItem>
{
    @Override
    public int compare(NotificationItem o1, NotificationItem o2) {
        return (int) (o2.value - o1.value);
    }
}