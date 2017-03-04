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
        data.add(new Point(23.794403, 90.401070, "Airport Road (Dhaka-Mymensingh Hwy) and Kemal Ataturk Avenue", null));
        data.add(new Point(23.850420, 90.408418, "Airport Road Roundabout", null));
        data.add(new Point(23.746015, 90.394651, "Bangla Motor Mor", null));
        data.add(new Point(23.743643, 90.382264, "Dhanmondi 6", null));
        data.add(new Point(23.780351, 90.416731, "Gulshan Circle 1", null));
        data.add(new Point(23.873833, 90.400593, "Uttara Housebuilding", null));
        data.add(new Point(23.775280, 90.389939, "Jahangir Gate", null));
        data.add(new Point(23.737625, 90.405229, "Kakrail Circle", null));
        data.add(new Point(23.737570, 90.409018, "Kakrail Road", null));
        data.add(new Point(23.738111, 90.395851, "Kazi Nazrul Islam Avenue & Shahbagh", null));
        data.add(new Point(23.764426, 90.389003, "Kazi Nazrul Islam Avenue & Bijoy Sharani", null));
        data.add(new Point(23.758477, 90.389871, "Kazi Nazrul Islam Avenue & Indira Road", null));
        data.add(new Point(23.758442, 90.383746, "Khamar Bari Gol Chottor", null));
        data.add(new Point(23.828733, 90.420070, "Khilkhet", null));
        data.add(new Point(23.744144, 90.414286, "Malibag Mor", null));
        data.add(new Point(23.750099, 90.413043, "Malibag Rail Gate", null));
        data.add(new Point(23.760161, 90.372976, "Mirpur Road & Asad Avenue", null));
        data.add(new Point(23.738768, 90.383448, "Mirpur Road & Elephant Road", null));
        data.add(new Point(23.758307, 90.374220, "Mirpur Road & Manik Mia Avenue", null));
        data.add(new Point(23.756349, 90.375102, "Mirpur Road & Old Dhanmondi 27/New 16", null));
        data.add(new Point(23.778311, 90.397932, "Mohakhali Chourasta", null));
        data.add(new Point(23.745760, 90.412240, "Mouchak", null));
        data.add(new Point(23.751346, 90.378314, "Panthapath & Mirpur Road", null));
        data.add(new Point(23.767700, 90.423000, "Rampura Bridge - DIT road <-> Hatirjheel", null));
        data.add(new Point(23.749859, 90.393158, "SAARC Fountain (Sonargaon, Bashundhara City Shopping Complex)", null));
        data.add(new Point(23.741595, 90.411856, "Shantinagar Mor", null));
        data.add(new Point(23.768240, 90.382861, "Zia Udyan", null));
        data.add(new Point(23.738348, 90.372999, "Zigatala", null));
        data.add(new Point(23.794847, 90.414213, "Gulshan Circle 2", null));
        data.add(new Point(23.797715, 90.423513, "Notun Bazar", null));
        data.add(new Point(23.809595, 90.421341, "Nadda", null));
        data.add(new Point(23.811952, 90.421267, "Bashundhara Gate", null));
        data.add(new Point(23.818811, 90.414887, "Shewra", null));
        data.add(new Point(23.796625, 90.407494, "Banani Rd 27", null));
        data.add(new Point(23.799114, 90.401882, "Banani Graveyard Road", null));
        data.add(new Point(23.793589, 90.408569, "Banani Rd 27 - Kemal Ataturk", null));
        data.add(new Point(23.791370, 90.400473, "Banani 11 - Kakoli", null));
        data.add(new Point(23.790369, 90.407899, "Banani 11", null));
        data.add(new Point(23.789921, 90.411179, "Banani 11", null));
        data.add(new Point(23.780717, 90.425624, "Link Road - Pragati Sharani", null));
        data.add(new Point(23.771430, 90.425226, "Merul Badda", null));
        data.add(new Point(23.768240, 90.423319, "Rampura Bridge", null));
        data.add(new Point(23.765491, 90.421816, "Rampura", null));
        data.add(new Point(23.754172, 90.415417, "Khilgaon Abul Hotel", null));
        data.add(new Point(23.790051, 90.416157, "Azad Masjid", null));
        data.add(new Point(23.774244, 90.416132, "Gulshan Shooting Complex", null));
        data.add(new Point(23.780535, 90.405433, "WireLess Mor", null));
        data.add(new Point(23.787986, 90.399832, "Chariman Bari", null));
        data.add(new Point(23.790517, 90.400307, "Shainik Club Bus Stop", null));
        data.add(new Point(23.750426, 90.390355, "Bashundhara City Complex", null));
        data.add(new Point(23.751050, 90.387090, "Panthapath - Green Road ", null));
        data.add(new Point(23.741434, 90.396089, "Shahbagh / Mintu Road Intersection", null));
        data.add(new Point(23.822576, 90.419733, "Nikunja", null));
        data.add(new Point(23.756918, 90.398946, "Satrastar Mor", null));
        data.add(new Point(23.770088, 90.401091, "Nabiscor Mor", null));
        data.add(new Point(23.763684, 90.400059, "Bijoy Sarani - Tejgaon Link Rd", null));
        data.add(new Point(23.744175, 90.405121, "Minto Rd", null));
        data.add(new Point(23.745162, 90.404196, "Old Elephant Road", null));
        data.add(new Point(23.753424, 90.400733, "Sonargaon Road (Entrance to Karwan Bazar)", null));

    }

    public List<Point> getPoints ()
    {
        return data;
    }
}
