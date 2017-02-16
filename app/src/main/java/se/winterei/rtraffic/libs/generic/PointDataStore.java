package se.winterei.rtraffic.libs.generic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by reise on 2/16/2017.
 */

public class PointDataStore
{
    private  List<Point> data = new ArrayList<>();

    public PointDataStore ()
    {
        data.add(new Point(23.794403, 90.401070, "Airport Road (Dhaka-Mymensingh Hwy) and Kemal Ataturk Avenue", null, Point.state.CONGESTED));
        data.add(new Point(23.850420, 90.408418, "Airport Road Roundabout", null, Point.state.SLOW_BUT_MOVING));
        data.add(new Point(23.746015, 90.394651, "Bangla Motor Mor", null, Point.state.UNCONGESTED));
        data.add(new Point(23.743643, 90.382264, "Dhanmondi 6", null, Point.state.SLOW_BUT_MOVING));
        data.add(new Point(23.780351, 90.416731, "Gulshan Circle 1", null, Point.state.CONGESTED));
        data.add(new Point(23.873833, 90.400593, "Uttara Housebuilding", null, Point.state.UNCONGESTED));
        data.add(new Point(23.775280, 90.389939, "Jahangir Gate", null, Point.state.SLOW_BUT_MOVING));
        data.add(new Point(23.737625, 90.405229, "Kakrail Circle", null, Point.state.CONGESTED));
        data.add(new Point(23.737570, 90.409018, "Kakrail Road", null, Point.state.CONGESTED));
        data.add(new Point(23.738111, 90.395851, "Kazi Nazrul Islam Avenue & Shahbagh", null, Point.state.SLOW_BUT_MOVING));
        data.add(new Point(23.764426, 90.389003, "Kazi Nazrul Islam Avenue & Bijoy Sharani", null, Point.state.UNCONGESTED));
        data.add(new Point(23.758477, 90.389871, "Kazi Nazrul Islam Avenue & Indira Road", null, Point.state.CONGESTED));
        data.add(new Point(23.758442, 90.383746, "Khamar Bari Gol Chottor", null, Point.state.UNCONGESTED));
        data.add(new Point(23.828733, 90.420070, "Khilkhet", null, Point.state.SLOW_BUT_MOVING));
        data.add(new Point(23.744144, 90.414286, "Malibag Mor", null, Point.state.CONGESTED));
        data.add(new Point(23.750099, 90.413043, "Malibag Rail Gate", null, Point.state.UNCONGESTED));
        data.add(new Point(23.760161, 90.372976, "Mirpur Road & Asad Avenue", null, Point.state.SLOW_BUT_MOVING));
        data.add(new Point(23.738768, 90.383448, "Mirpur Road & Elephant Road", null, Point.state.CONGESTED));
        data.add(new Point(23.758307, 90.374220, "Mirpur Road & Manik Mia Avenue", null, Point.state.UNCONGESTED));
        data.add(new Point(23.756349, 90.375102, "Mirpur Road & Old Dhanmondi 27/New 16", null, Point.state.SLOW_BUT_MOVING));
        data.add(new Point(23.778311, 90.397932, "Mohakhali Chourasta", null, Point.state.CONGESTED));
        data.add(new Point(23.745760, 90.412240, "Mouchak", null, Point.state.UNCONGESTED));
        data.add(new Point(23.751346, 90.378314, "Panthapath & Mirpur Road", null, Point.state.SLOW_BUT_MOVING));
        data.add(new Point(23.767700, 90.423000, "Rampura Bridge - DIT road <-> Hatirjheel", null, Point.state.UNCONGESTED));
        data.add(new Point(23.749859, 90.393158, "SAARC Fountain (Sonargaon, Bashundhara City Shopping Complex)", null, Point.state.CONGESTED));
        data.add(new Point(23.741595, 90.411856, "Shantinagar Mor", null, Point.state.SLOW_BUT_MOVING));
        data.add(new Point(23.768240, 90.382861, "Zia Udyan", null, Point.state.UNCONGESTED));
        data.add(new Point(23.738348, 90.372999, "Zigatala", null, Point.state.SLOW_BUT_MOVING));
        data.add(new Point(23.794847, 90.414213, "Gulshan Circle 2", null, Point.state.CONGESTED));
    }

    public List<Point> getPoints ()
    {
        return data;
    }
}
