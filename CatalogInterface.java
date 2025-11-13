import java.sql.*;
import java.util.Scanner;
public class CatalogInterface
{
    private static final Scanner scan = new Scanner(System.in);
    public static void catalogInterface(Connection conn)
    {
        catalogWelcome();

        int catalogSelection = 0;
        while (catalogSelection != 4)
        {
            catalogSelection = catalogSelection();

            switch(catalogSelection)
            {
                case 1:
                    
                    businessCatalog(conn);
                    break;
                case 2:
                    individualCatalog(conn);
                    break;
                case 3:
                    individualCatalog(conn);
                    businessCatalog(conn);
                    break;
            }
        }
        
        

    }

    public static void catalogWelcome()
    {
        System.out.println(" _____ _                       _____       _        _             ");
        System.out.println("|_   _| |                     /  __ \\     | |      | |            ");
        System.out.println("  | | | |_ ___ _ __ ___  ___  | /  \\/ __ _| |_ __ _| | ___   __ _ ");
        System.out.println("  | | | __/ _ \\ '_ ` _ \\/ __| | |    / _` | __/ _` | |/ _ \\ / _` |");
        System.out.println(" _| |_| ||  __/ | | | | \\__ \\ | \\__/\\ (_| | || (_| | | (_) | (_| |");
        System.out.println(" \\___/ \\__\\___|_| |_| |_|___/  \\____/\\__,_|\\__\\__,_|_|\\___/ \\__, |");
        System.out.println("                                                             __/ |");
        System.out.println("                                                            |___/ ");
    }
    public static int catalogSelection()
    {
        String[] catalogOptions = {
            "The Business Catalog",
            "The Individual Catalog",
            "The Manager Catalog",
            "Go Back"
        };

        StringBuilder prompt = Main.chooseFromOptions("Please enter a number to choose one of the following options", catalogOptions);
        int entry = Main.getIntInRange(prompt,1,4);
        return entry;
    }


    public static void individualCatalog(Connection conn)
    {
        String statement = "SELECT description, price, vendor, warranty_length FROM item";
        try(PreparedStatement statementVal =  conn.prepareStatement(statement))
        {
            try(ResultSet rs = statementVal.executeQuery())
            {
                System.out.printf("%-30s %-11s %-25s %15s%n", "Description", "Price", "Vendor", "Warranty (months)");

                while(rs.next())
                {
                    System.out.printf("%-30s $%-10.2f %-25s %15d%n",rs.getString("description"), rs.getDouble("price"), rs.getString("vendor"),rs.getInt("warranty_length")
                    );
                }
            }   
            catch(SQLException ex)
            {
                System.out.println("Error executing individual catalog gathering: " +  ex.getMessage());
            }
        }
        catch(SQLException ex)
        {
            System.out.println("Error executing individual catalog gathering: " +  ex.getMessage());
        }
    }   

    public static void businessCatalog(Connection conn)
    {
        String statement = "SELECT description, price, vendor, duration * 60 as duration from service";
        try(PreparedStatement statementVal =  conn.prepareStatement(statement))
        {

            try(ResultSet rs = statementVal.executeQuery())
            {
                System.out.printf("%-30s %-11s %-25s %13s%n", "Description", "Price", "Vendor", "Duration");

                while(rs.next())
                {
                    System.out.printf("%-30s $%-10.2f %-25s %8d mins%n", rs.getString("description"), rs.getDouble("price"), rs.getString("vendor"), rs.getInt("duration"));
                }

                
            }   
            catch(SQLException ex)
            {
                System.out.println("Error executing business catalog gathering" +  ex.getMessage());
            }
        }
        catch(SQLException ex)
        {
            System.out.println("Error executing business catalog gathering" +  ex.getMessage());
        }
    }   


    public static void businessInterface(Connection conn)
    {

        int customerID = CustomerHelpers.signUserIn(conn, 2);

        System.out.println("Welcome to the business Interface.");



        String[] catalogOptions = {
            "Make Purchase",
            "Surf Catalog",
            "Go Back"
        };

        StringBuilder prompt = Main.chooseFromOptions("Please enter a number to choose one of the following options", catalogOptions);
        int entry = -1;
        
        while(entry != 3)
        {
            entry = Main.getIntInRange(prompt,1,3);

            switch(entry)
            {
                case 1:
                    makeServicePurchase(conn, customerID);
                    break;
                case 2:
                    System.out.println("Services Catalog:\n");
                    businessCatalog(conn);
                    break;
            }
        }
    }

    public static void makeServicePurchase(Connection conn, int customerId)
    {
        businessCatalog(conn);
        String prompt = "Please enter the FULL NAME of an item above:";
        String sqlQ = "SELECT product_id from service where description = ?";
        int productId = getItemId(prompt, conn, sqlQ, "service");

        int paymentId = -1;
        
        while(paymentId < 0)
        {
            CustomerHelpers.getCustomerPaymentMethods(customerId, conn, false);
            paymentId = selectCustomerPaymentMethods(customerId, conn, false);
        }

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        

        String insertSql = "INSERT INTO Bus_Transaction (date_of_order, wire_success_status, pay_id, product_id, customer_id) VALUES (?,'SUCCESS',?,?,?)";

        try(PreparedStatement insertionToTrans = conn.prepareStatement(insertSql))
        {
            insertionToTrans.setDate(1, today);
            insertionToTrans.setInt(2, paymentId);
            insertionToTrans.setInt(3, productId);
            insertionToTrans.setInt(4, customerId);

            insertionToTrans.executeUpdate();

        }
        catch(SQLException e)
        {
            System.out.println("Error Inserting transaction");
        }

    }

    










































public static int getBankIdFromUser() 
{
    int bankId = -1;
    while (true) 
    {
        System.out.print("Please enter your Bank ID: ");
        String input = scan.nextLine().trim();
        try 
        {
            bankId = Integer.parseInt(input);
            if (bankId > 0) 
            {
                break;
            } 
            else 
            {
                System.out.println("Bank ID must be a positive integer.");
            }
        } 
        catch (NumberFormatException e)
        {
            System.out.println("Invalid input. Please enter a valid integer Bank ID.");
        }
    }
    return bankId;
}




public static int selectCustomerPaymentMethods(int userId,Connection conn, boolean isInd)
{
        if(isInd)
        {

        }
        // {

        //     System.out.println("\n------------------------Customer Payment Information------------------------");
        //     String creditCardQuery = "WITH payments AS (SELECT * from payment where user_id = ?) SELECT * from credit_card where pay_id in (SELECT pay_id from payments)";

        //     String bankAccountQuery = "WITH payments AS (SELECT * from payment where user_id = ?) SELECT * from bank_account where pay_id in (SELECT pay_id from payments)";
        //     try(PreparedStatement creditInformation = conn.prepareStatement(creditCardQuery);
        //         PreparedStatement bankInformation = conn.prepareStatement(bankAccountQuery);)

        //     {
        //         creditInformation.setInt(1, userId);
        //         bankInformation.setInt(1, userId);

        //         try(ResultSet rs = creditInformation.executeQuery())
        //         {
        //             if(!rs.next())
        //             {
        //                 System.out.println("\n------------------------Credit Cards on File------------------------");
        //                 System.out.println("No Credit Cards on File");
        //             }
        //             else
        //             {
        //                 System.out.println("\n------------------------Credit Cards on File------------------------");
        //                 System.out.println(String.format("%-4s%-20s%-12s%-40s", "No.", "Card Number", "Exp. Date", "Billing Address"));
        //                 int i = 1;
        //                 do {
        //                     String cardNumber = rs.getString("card_number");
        //                     String expiry = rs.getString("expiration_date");
        //                     String billing_address = rs.getString("billing_address");

        //                     //Format expiry as MM/YYYY
        //                     String formattedExpiry = expiry;
        //                     if (expiry != null && expiry.length() >= 7) {
        //                         //Extract year and month from format YYYY-MM-DD
        //                         String year = expiry.substring(0, 4);
        //                         String month = expiry.substring(5, 7);
        //                         formattedExpiry = month + "/" + year;
        //                     }

        //                     // Add spaces after commas in billing address
        //                     String formattedAddress = billing_address.replaceAll(",", ", ");

        //                     System.out.println(String.format("%-4s%-20s%-12s%-40s", i + ".", cardNumber, formattedExpiry, formattedAddress));
        //                     i++;
        //                 } while(rs.next());
        //             }
        //         }
        //         catch(SQLException e)
        //         {
        //             System.out.println("Error getting information about credit cards in sql");
        //             System.exit(-1);
        //         }

        //         try(ResultSet rs = bankInformation.executeQuery())
        //         {
        //             System.out.println("\n------------------------Bank Accounts on File------------------------");
        //             if(!rs.next())
        //             {
        //                 System.out.println("No Bank Accounts on File\n");
        //             }
        //             else
        //             {
        //                 System.out.println(String.format("%-4s%-20s%-20s%-20s", "No.", "Account Number", "Bank", "Routing Number"));
        //                 int i = 1;
        //                 do {
        //                     String accountNumber = rs.getString("account_number");
        //                     String bankName = rs.getString("bank");
        //                     String routingNumber = rs.getString("routing_number");
        //                     System.out.println(String.format("%-4s%-20s%-20s%-20s", i + ".", accountNumber, bankName, routingNumber));
        //                     i++;
        //                 } while(rs.next());
        //             }
        //         }
        //         catch(SQLException e)
        //         {
        //             System.out.println("Error getting information about bank accounts in sql");
        //             System.exit(-1);
        //         }

        //     }
        //     catch(SQLException e)
        //     {
        //         System.out.println("Unexpected SQL Error in getting Customer Payments");
        //         System.exit(-1);
        //     }
        // }
        else
        {

            String bankAccountSelectionQuery = "WITH payments AS (SELECT * from payment where user_id = ?) SELECT * from bank_account where pay_id in (SELECT pay_id from payments) and pay_id = ?";
            try(PreparedStatement bankInformation = conn.prepareStatement(bankAccountSelectionQuery);)
            {
                bankInformation.setInt(1, userId);
                
                int bankId = getBankIdFromUser();
                bankInformation.setInt(2, bankId);


                try(ResultSet rs = bankInformation.executeQuery())
                {
                    if(!rs.next())
                    {
                        System.out.println("Error: You do not own that bank account. Please try Again");
                        return -1;
                    }
                    else
                    {
                        return bankId;
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
        return -1;
    }




    public static int getItemId(String prompt, Connection conn, String sql, String type)
    {
        System.out.println(prompt);
        String userInput = scan.nextLine();

        try(PreparedStatement pstat = conn.prepareStatement(sql);)
        {
            pstat.setString(1,userInput);
           
            try(ResultSet rs = pstat.executeQuery())
            {
                if(rs.next())
                {
                    int product_id = rs.getInt("product_id");
                    return product_id;
                }
                else 
                {
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
    
}