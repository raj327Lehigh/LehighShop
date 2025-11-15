import java.sql.*;
import java.util.Scanner;

public class ManagerInterface
{
    private static final Scanner scan = new Scanner(System.in);
    public static void managerInterface(Connection conn)
    {
        managerWelcome(conn);

        String[] catalogOptions = 
        {
            "Generate Reports",
            "Add Financing Options",
            "Go Back"
        };

        StringBuilder prompt = Main.chooseFromOptions("Please enter a number to choose one of the following options", catalogOptions);
        int entry = -1;
        
        while(entry != 3)
        {
            entry = Main.getIntInRange(prompt, 1, 3);
            
            switch(entry)
            {
                case 1:
                    reports();
                    break;
                case 2:
                    break;
            }
        }

    }


    public static void managerWelcome(Connection conn)
    {

        CustomerHelpers.currentManagerList(conn);

        int customerId = -1;
        while(customerId == -1)
        {
            customerId = CustomerHelpers.signUserIn(conn,3);
        }


        System.out.println(" _    _      _                           ___  ___                                  ");
        System.out.println("| |  | |    | |                          |  \\/  |                                  ");
        System.out.println("| |  | | ___| | ___ ___  _ __ ___   ___  | .  . | __ _ _ __   __ _  __ _  ___ _ __ ");
        System.out.println("| |\\/| |/ _ \\ |/ __/ _ \\| '_ ` _ \\ / _ \\ | |\\/| |/ _` | '_ \\ / _` |/ _` |/ _ \\ '__|");
        System.out.println("\\  /\\  /  __/ | (_| (_) | | | | | |  __/ | |  | | (_| | | | | (_| | (_| |  __/ |   ");
        System.out.println(" \\/  \\/ \\___|_|\\___\\___/|_| |_| |_|\\___| \\_|  |_/\\__,_|_| |_|\\__,_|\\__, |\\___|_|   ");
        System.out.println("                                                                    __/ |          ");
        System.out.println("                                                                   |___/           ");

    }


    public static void reports(Connection conn)
    {

        String[] catalogOptions = 
        {
            "Highest Spender Report",
            "Highest Selling Items",
            "Oustanding Financing Report",
            "Go Back"
        };

        StringBuilder prompt = Main.chooseFromOptions("Please enter a number to choose one of the following options", catalogOptions);
        int entry = -1;
        while(entry != 4)
        {
            entry = Main.getIntInRange(prompt, 1, 4);
            
            switch(entry)
            {
                case 1:
                    reports();
                    break;
                case 2:
                    break;
            }
        }
    }

    //LeaderBoard for most spending
    //Most Sold Items
    //Outstanding Financing Report 
}