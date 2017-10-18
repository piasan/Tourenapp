package de.hochschule_trier.tourenapp;

import android.location.Location;

public class TourIndex {

    public static long getIndex(double latitude, double longitude) {

        //remove decimals
        latitude *= 10;
        longitude *= 10;

        long index;

        //East
        if (longitude >= 0) {

            //North
            if (latitude >= 0) {
                index = (899 - (long) latitude) * 3600 + (long) longitude; //N 0-89, E 0-179
            }

            //South
            else {
                index = (900 - (long) latitude) * 3600 + (long) longitude; //S 90-179  E 0-179
            }

        }

        //West
        else {

            //North
            if (latitude >= 0) {
                index = (899 - (long) latitude) * 3600 + (3599 + (long) longitude); //N 0-89, W 180-359
            }

            //South
            else {
                index = (900 - (long) latitude) * 3600 + (3599 + (long) longitude); //S 90-179, W 180 -359
            }

        }

        return index;

    }

    public long getLeftNeighbor(long index) {

        if (index % 3600 != 0)
            return index - 1;
        else
            return index + 359;
    }

    public long getRightNeighbor(long index) {

        if (index % 3600 != 3599)
            return index + 1;
        else
            return index - 3599;
    }

    public long getTopNeighbor(long index) {

        if (index > 3599)
            return index - 3600;
        else
            return -1;
    }

    public long getBottomNeighbor(long index) {

        if (index <= 6476399) //(1800*3600) - 3599
            return index + 3600;
        else
            return -1;

    }
}


