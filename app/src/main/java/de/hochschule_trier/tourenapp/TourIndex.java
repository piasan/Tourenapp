package de.hochschule_trier.tourenapp;

import android.location.Location;

public class TourIndex {

    public static long getIndex(Location loc) {

        //remove decimals
        double latitude = loc.getLatitude();
        double longitude =  loc.getLongitude();

        long index;

        //East
        if (longitude >= 0) {

            //North
            if (latitude >= 0) {
                index = (89 - (long)latitude) * 360 + (long)longitude; //N 0-89, E 0-179
            }

            //South
            else {
                index = (90 - (long)latitude) * 360 + (long)longitude; //S 90-179  E 0-179
            }

        }

        //West
        else {

            //North
            if (latitude >= 0) {
                index = (89 - (long)latitude) * 360 + (359 + (long)longitude); //N 0-89, W 180-359
            }

            //South
            else {
                index = (90 - (long)latitude) * 360 + (359 + (long)longitude); //S 90-179, W 180 -359
            }

        }

        return index;

    }
}
