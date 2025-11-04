import java.sql.*;
import java.util.Scanner;

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
            int interfaceNum = chooseInterface();

            if(interfaceNum == 1)
            {
                customerInterface(conn);
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


        StringBuilder prompt = chooseFromOptions("Please select a number to choose the group you belong to", mainOptions);

        int choiceOfCustomerInterface = getIntInRange(prompt, 1,5);

        if(choiceOfCustomerInterface == 1)
        {
            individualInterface(conn);
        }







       
    }

    public static void individualInterface(Connection conn)
    {
        
        currentUserList(conn); //Lists the current users with a nice message
        int customerID = signIndividualIn(conn); //Signs the user in and returns the customer ID
        String name = getName(conn, customerID); //gets the customers name for use in the appearance

        System.out.println("\n\n-------------Welcome " + name + "-------------");

        System.out.println("What would you like to do with your account?\n");

        String[] accountOptions =
        {
            "See Payment Methods",
            "Add a Payment Method",
            "See Purchases",
            "See Financing History",
            "See Total Expenses",
            "Exit"
        };

        StringBuilder promptForAccount = chooseFromOptions("Please select one of the following options", accountOptions);
        int choiceOfCustomerInterface = getIntInRange(promptForAccount, 1,6);

        switch(choiceOfCustomerInterface)
        {
            case 1:
                getCustomerPaymentMethods(customerID,conn, true);
                break;
            case 2:
                //createPaymentMethod(customerID, 1);
                break;
            case 3:
                //seePurchaseHistory(customerID, 1);
                break;
            case 4:
                //seeFinancingHistory(customerID);
                break;
            case 5:
                //seeTotalExpenses(customerID, 1);
                break;
            case 6:
                //TO DO IMPLEMENT LOOPING LOGIC 
                break;
        }



    }

    public static void currentUserList(Connection conn)
    {
        System.out.println("\n\nCurrent Individual Customer names: ");

        String sql = "SELECT * FROM individual JOIN customer USING (customer_id)";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) 
        {
            int i = 1;
            while (rs.next()) 
            {
                String name = rs.getString("name");
                String numDot = i + ".";
                System.out.println(String.format("%-4s%s", numDot, name));
                i++;
            }
        } 
        catch (SQLException e) 
        {
            System.out.println("Error fetching customers: " + e.getMessage());
        }

        System.out.println();
    }

    public static int signIndividualIn(Connection conn)
    {
        int customerID = -1;

        while(customerID == -1)
        {
            String customerPrompt = "Please Enter the FULL NAME of the customer you would like to sign in as from the list above:";
            String sqlQ = "SELECT customer_id from customer where name = ?";
            customerID = signIn(customerPrompt, conn, sqlQ, "individual");
        }

        return customerID;
    }

    public static int signIn(String prompt, Connection conn, String sql, String type)
    {
        System.out.print(prompt);    
        String userInput = scan.nextLine();
        try(PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, userInput);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) 
                {
                    int customer_id = rs.getInt("customer_id");
                    return customer_id;
                } 
                else {
                    System.out.println("Error: No matching " + type + " found");
                    return -1;
                }
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error Developing SQL prepared Statement, please try again");
            return -1;

        }
    }

    public static String getName(Connection conn, int customerID)
    {
        String nameStatement = "SELECT name FROM customer where customer_id = " + customerID;
        String name = null; 


        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(nameStatement)) 
        {
            if(rs.next())
            {
                name = rs.getString("name");
                return name;
            }
            else
            {
                System.out.println("An unforseen Erorr Occurred HERE");
                System.exit(-1);
            }
        } 
        catch (SQLException e) 
        {
            System.out.println("Error Message "+  e.getMessage());
            System.out.println("An unforseen Erorr Occurred");
            System.exit(-1);
        }
        return null;
    }

    public static void getCustomerPaymentMethods(int userId,Connection conn, boolean isCustomer)
    {
        if(isCustomer)
        {

            System.out.println("\n------------Customer Payment Information------------");
            String creditCardQuery = "WITH payments AS (SELECT * from payment where user_id = ?) SELECT * from credit_card where pay_id in (SELECT pay_id from payments)";

            String bankAccountQuery = "WITH payments AS (SELECT * from payment where user_id = ?) SELECT * from bank_account where pay_id in (SELECT pay_id from payments)";
            try(PreparedStatement creditInformation = conn.prepareStatement(creditCardQuery);
                PreparedStatement bankInformation = conn.prepareStatement(bankAccountQuery);)

            {
                creditInformation.setInt(1, userId);
                bankInformation.setInt(1, userId);

                try(ResultSet rs = creditInformation.executeQuery())
                {
                    if(!rs.next())
                    {
                        System.out.println("\n------------Credit Cards on File------------");
                        System.out.println("No Credit Cards on File");
                    }
                    else
                    {
                        System.out.println("\n------------Credit Cards on File------------");
                        System.out.println(String.format("%-4s%-20s%-12s%-40s", "No.", "Card Number", "Exp. Date", "Billing Address"));
                        int i = 1;
                        do {
                            String cardNumber = rs.getString("card_number");
                            String expiry = rs.getString("expiration_date");
                            String billing_address = rs.getString("billing_address");

                            //Format expiry as MM/YYYY
                            String formattedExpiry = expiry;
                            if (expiry != null && expiry.length() >= 7) {
                                //Extract year and month from format YYYY-MM-DD
                                String year = expiry.substring(0, 4);
                                String month = expiry.substring(5, 7);
                                formattedExpiry = month + "/" + year;
                            }

                            // Add spaces after commas in billing address
                            String formattedAddress = billing_address.replaceAll(",", ", ");

                            System.out.println(String.format("%-4s%-20s%-12s%-40s", i + ".", cardNumber, formattedExpiry, formattedAddress));
                            i++;
                        } while(rs.next());
                    }
                }
                catch(SQLException e)
                {
                    System.out.println("Error getting information about credit cards in sql");
                    System.exit(-1);
                }

                try(ResultSet rs = bankInformation.executeQuery())
                {
                    System.out.println("\n------------Bank Accounts on File------------");
                    if(!rs.next())
                    {
                        System.out.println("No Bank Accounts on File");
                    }
                    else
                    {
                        System.out.println(String.format("%-4s%-20s%-20s%-20s", "No.", "Account Number", "Bank", "Routing Number"));
                        int i = 1;
                        do {
                            String accountNumber = rs.getString("account_number");
                            String bankName = rs.getString("bank");
                            String routingNumber = rs.getString("routing_number");
                            System.out.println(String.format("%-4s%-20s%-20s%-20s", i + ".", accountNumber, bankName, routingNumber));
                            i++;
                        } while(rs.next());
                    }
                }
                catch(SQLException e)
                {
                    System.out.println("Error getting information about bank accounts in sql");
                    System.exit(-1);
                }

            }
            catch(SQLException e)
            {
                System.out.println("Unexpected SQL Error in getting Customer Payments");
                System.exit(-1);
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

