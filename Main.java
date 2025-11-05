import java.sql.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;

public class Main {

    private static final Scanner scan = new Scanner(System.in);

    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@//rocordb01.cse.lehigh.edu:1522/cse241pdb";

        Connection temp = null;

        while (temp == null) {
            System.out.print("Enter Oracle user id: ");
            String user = scan.nextLine().trim();

            System.out.print("Enter Oracle password: ");
            String password = scan.nextLine().trim();

            try 
            {
                temp = DriverManager.getConnection(url, user, password);
                System.out.println("\nConnection successful!\n");
            } 
            catch (SQLException e) 
            {
                System.out.println("\nConnection failed. Please check your username or password.\n");
            }
        }

        try (Connection conn = temp) 
        {
            introMessage();
            int interfaceNum = -1;

            while(interfaceNum != 4)
            {
                interfaceNum = chooseInterface();
                if(interfaceNum == 1)
                {
                    customerInterface(conn);
                }
            }
            
            
        }
        catch (SQLException e) 
        {
            System.out.println("Error while closing connection: " + e.getMessage());
        }
    }

    //actually implemented user functions in order of use
    
    public static void introMessage()
    {
                
        System.out.println("    __         __    _       __       _____ __              ");
        System.out.println("   / /   ___  / /_  (_)___ _/ /_     / ___// /_  ____  ____ ");
        System.out.println("  / /   / _ \\/ __ \\/ / __ `/ __ \\    \\__ \\/ __ \\/ __ \\/ __ \\");
        System.out.println(" / /___/  __/ / / / / /_/ / / / /   ___/ / / / / /_/ / /_/ /");
        System.out.println("/_____/\\___/_/ /_/_/\\__, /_/ /_/   /____/_/ /_/\\____/ .___/ ");
        System.out.println("                   /____/                          /_/       "); //gernerated ascii art with https://patorjk.com/software/taag/#p=display&f=Slant&t=Lehigh+Shop&x=none&v=4&h=4&w=80&we=false

    }
    public static int chooseInterface()
    {
        boolean prompting = true;
            
        String[] mainOptions = {
            "The Customer Interface",
            "The Catalog Interface",
            "The Manager Interface",
            "Exit"
        };

        StringBuilder prompt = chooseFromOptions("Please enter a number to choose one of the following options", mainOptions);
        return getIntInRange(prompt,1,4);

    }

    public static void customerInterface(Connection conn)
    {

        System.out.println("\n\n************Welcome to the Customer Interface!************\n\n");

            
        String[] mainOptions =
        {
            "Individual Customer",
            "Business Customer",
            "New Individual Customer",
            "New Business Customer",
            "Go Back"
        };

        int choiceOfCustomerInterface = -1;
        while(choiceOfCustomerInterface != 5)
        {
            StringBuilder prompt = chooseFromOptions("Please select a number to choose the group you belong to", mainOptions);

            choiceOfCustomerInterface = getIntInRange(prompt, 1,5);

            if(choiceOfCustomerInterface == 1)
            {
                IndividualCustomer.individualInterface(conn);
            }
            else if(choiceOfCustomerInterface == 2)
            {
                //TODOD BusinessCustomer.businessInterface(conn);
            }
            else if(choiceOfCustomerInterface == 3)
            {
                // TODO CreateIndividualCustomer.individualCreation(conn);
            }
            else if(choiceOfCustomerInterface == 4)
            {
                //TO DO CreateBusinessCustomer.businessCreation(conn);
            }
        }

       
    }

    //Helper Functions 

    //Prompts a user for an integer and asks until they give an integer in the valid range.
    public static int getIntInRange(StringBuilder prompt, int min, int max) 
    {
        while (true) 
        {
            System.out.print(prompt);
            String input = scan.nextLine().trim(); //scan the next line in and if this valid 
            try 
            {
                int value = Integer.parseInt(input); //try to parse it as int and if this valid 
                if (value >= min && value <= max) //make sure the integer is in the valid range 
                {
                    return value;
                } 
                else 
                {
                    System.out.println("Please enter a number between " + min + " and " + max); //Please enter a number in this range
                }
            } 
            catch (NumberFormatException e) 
            {
                System.out.println("Please enter an integer.");
            }
        }
    }

    public static StringBuilder chooseFromOptions(String prompt, String[] options) 
    {
        StringBuilder menu = new StringBuilder(prompt + "\n");
        for (int i = 0; i < options.length; i++) 
        {
            menu.append((i + 1) + ". " + options[i] + "\n");
        }
        menu.append("\nMake your selection here: ");
        return menu;
    }

}

