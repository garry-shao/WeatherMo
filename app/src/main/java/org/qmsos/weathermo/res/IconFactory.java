package org.qmsos.weathermo.res;

import org.qmsos.weathermo.R;

/**
 * Set weather icon to TextView, usually those acted as forecast view.
 */
public class IconFactory {

    /**
     * Get resource id of specified weather.
     *
     * @param weatherId
     *            The id of specified weather.
     * @return The corresponding resource id, or 0 if invalid.
     */
    public static int getWeatherIcon(int weatherId) {
        int resId;

        switch (weatherId) {
        case 200:
        case 201:
        case 202:
        case 210:
        case 211:
        case 212:
        case 221:
        case 230:
        case 231:
        case 232:
            resId = R.drawable.ic_thunderstorm;
            break;
        case 300:
        case 301:
        case 302:
        case 310:
        case 311:
        case 312:
        case 313:
        case 314:
        case 321:
            resId = R.drawable.ic_drizzle;
            break;
        case 500:
        case 501:
            resId = R.drawable.ic_rain_light;
            break;
        case 502:
        case 503:
        case 504:
            resId = R.drawable.ic_rain_heavy;
            break;
        case 511:
            resId = R.drawable.ic_rain_freezing;
            break;
        case 520:
        case 521:
        case 522:
        case 531:
            resId = R.drawable.ic_rain_shower;
            break;
        case 600:
        case 601:
        case 602:
        case 620:
        case 621:
        case 622:
            resId = R.drawable.ic_snow;
            break;
        case 611:
        case 612:
        case 615:
        case 616:
            resId = R.drawable.ic_snow_rain;
            break;
        case 701:
        case 741:
            resId = R.drawable.ic_fog;
            break;
        case 711:
        case 721:
            resId = R.drawable.ic_haze;
            break;
        case 731:
        case 751:
        case 761:
        case 762:
            resId = R.drawable.ic_sand;
            break;
        case 771:
        case 781:
            resId = R.drawable.ic_squalls;
            break;
        case 800:
            resId = R.drawable.ic_sky_clear;
            break;
        case 801:
            resId = R.drawable.ic_clouds_few;
            break;
        case 802:
            resId = R.drawable.ic_clouds_scattered;
            break;
        case 803:
            resId = R.drawable.ic_clouds_broken;
            break;
        case 804:
            resId = R.drawable.ic_clouds_overcast;
            break;
        default:
            resId = 0;
        }

        return resId;
    }
}