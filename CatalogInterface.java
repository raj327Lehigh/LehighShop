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
                    businessInterface(conn);
                    break;
                case 2:
                    customerInterface(conn);
                    break;
                case 3:
                    managerInterface(conn);
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

        StringBuilder prompt = Main.chooseFromOptions("\nPlease enter a number to choose one of the following options", catalogOptions);
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
                    System.out.printf("%-30s $%-10.2f %-25s %15d%n",rs.getString("description"), rs.getDouble("price"), rs.getString("vendor"),rs.getInt("warranty_length"));
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
        System.out.println();
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
        System.out.println();

    }   


    public static void businessInterface(Connection conn)
    {
        CustomerHelpers.currentUserList(conn, false); 
        
        int customerID = -1;
        while(customerID == -1)
        {
            customerID = CustomerHelpers.signUserIn(conn, 2);
        }

        System.out.println("______           _                       _____      _             __               ");
        System.out.println("| ___ \\         (_)                     |_   _|    | |           / _|              ");
        System.out.println("| |_/ /_   _ ___ _ _ __   ___  ___ ___    | | _ __ | |_ ___ _ __| |_ __ _  ___ ___ ");
        System.out.println("| ___ \\ | | / __| | '_ \\ / _ \\/ __/ __|   | || '_ \\| __/ _ \\ '__|  _/ _` |/ __/ _ \\");
        System.out.println("| |_/ / |_| \\__ \\ | | | |  __/\\__ \\__ \\  _| || | | | ||  __/ |  | || (_| | (_|  __/");
        System.out.println("\\____/ \\__,_|___/_|_| |_|\\___||___/___/  \\___/_| |_|\\__\\___|_|  |_| \\__,_|\\___\\___|");
        System.out.println("                                                                                   ");
        System.out.println("                                                                                   ");

        System.out.println("\nWelcome to the Business Catalog Interface.");

        String[] catalogOptions = {
            "Make Purchase",
            "Surf Catalog",
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
            paymentId = selectCustomerPaymentMethods(customerId, conn, false, false);
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
            System.out.println("  _______ _                 _     __     __         ");
            System.out.println(" |__   __| |               | |    \\ \\   / /         ");
            System.out.println("    | |  | |__   __ _ _ __ | | __  \\ \\_/ /__  _   _ ");
            System.out.println("    | |  | '_ \\ / _` | '_ \\| |/ /   \\   / _ \\| | | |");
            System.out.println("    | |  | | | | (_| | | | |   <     | | (_) | |_| |");
            System.out.println("    |_|  |_| |_|\\__,_|_| |_|_|\\_\\    |_|\\___/ \\__,_|");

            System.out.println("\n\nThank you for your purchase! What would you like to do next?\n");

        }
        catch(SQLException e)
        {
            System.out.println("Error Inserting transaction: " + e.getMessage());
        }

    }


public static int getBankIdFromUser() 
{
    int bankId = -1;
    while (true) 
    {
        System.out.print("\nPlease enter the pay_id you would like to use: ");
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
                System.out.println("pay_id must be a positive integer.");
            }
        } 
        catch (NumberFormatException e)
        {
            System.out.println("Invalid input. Please enter a valid integer pay_id.");
        }
    }
    return bankId;
}


public static void managerInterface(Connection conn)
{
    CustomerHelpers.currentManagerList(conn);

    int customerId = -1;
    while(customerId == -1)
    {
        customerId = CustomerHelpers.signUserIn(conn,3);
    }

    System.out.println("___  ___                                    _____       _        _             ");
    System.out.println("|  \\/  |                                   /  __ \\     | |      | |            ");
    System.out.println("| .  . | __ _ _ __   __ _  __ _  ___ _ __  | /  \\/ __ _| |_ __ _| | ___   __ _ ");
    System.out.println("| |\\/| |/ _` | '_ \\ / _` |/ _` |/ _ \\ '__| | |    / _` | __/ _` | |/ _ \\ / _` |");
    System.out.println("| |  | | (_| | | | | (_| | (_| |  __/ |    | \\__/\\ (_| | || (_| | | (_) | (_| |");
    System.out.println("\\_|  |_/\\__,_|_| |_|\\__,_|\\__, |\\___|_|     \\____/\\__,_|\\__\\__,_|_|\\___/ \\__, |");
    System.out.println("                           __/ |                                          __/ |");
    System.out.println("                          |___/                                          |___/ ");

    System.out.println("\nWelcome to the Manager Catalog Interface.");

    String[] catalogOptions = {
        "Add Item",
        "Remove Item",
        "Surf Catalog",
        "Go Back"
    };

    StringBuilder prompt = Main.chooseFromOptions("\nPlease enter a number to choose one of the following options", catalogOptions);
    int entry = -1;
    
    while(entry != 4)
    {
        entry = Main.getIntInRange(prompt, 1, 4);
        
        switch(entry)
        {
            case 1:
                addItem(conn);
                break;
            case 2:
                removeItem(conn);
                break;
            case 3:
                showManagerItems(conn);
                break;
        }
    }

}

public static void customerInterface(Connection conn)
{
    CustomerHelpers.currentUserList(conn, true); 
        
    int customerID = -1;
    while(customerID == -1)
    {
        customerID = CustomerHelpers.signUserIn(conn, 1);
    }

    
    System.out.println(" _____           _                              _____       _        _              ");
    System.out.println("/  __ \\         | |                            /  __ \\     | |      | |             ");
    System.out.println("| /  \\/_   _ ___| |_ ___  _ __ ___   ___ _ __  | /  \\/ __ _| |_ __ _| | ___   __ _  ");
    System.out.println("| |   | | | / __| __/ _ \\| '_ ` _ \\ / _ \\ '__| | |    / _` | __/ _` | |/ _ \\ / _` | ");
    System.out.println("| \\__/\\ |_| \\__ \\ || (_) | | | | | |  __/ |    | \\__/\\ (_| | || (_| | | (_) | (_| | ");
    System.out.println(" \\____/\\__,_|___/\\__\\___/|_| |_| |_|\\___|_|     \\____/\\__,_|\\__\\__,_|_|\\___/ \\__, | ");
    System.out.println("                                                                              __/ |  ");
    System.out.println("                                                                             |___/   ");

    System.out.println("\nWelcome to the Customer Catalog Interface.");

    String[] catalogOptions = {
        "Make Unfinanced Purchase",
        "Make Financed Purchase",
        "Make Financed Payment",
        "Surf Catalog",
        "Go Back"
    };

    StringBuilder prompt = Main.chooseFromOptions("\nPlease enter a number to choose one of the following options", catalogOptions);
    int entry = -1;
    
    while(entry != 5)
    {
        entry = Main.getIntInRange(prompt, 1, 5);
        
        switch(entry)
        {
            case 1:
                makeUnfinancedPurchase(conn, customerID);
                break;
            case 2:
                makeFinancedPurchase(conn, customerID);
                break;
            case 3:
                makePaymentTowardsFinancing(conn, customerID);
                break;
            case 4:
                System.out.println("Customer Catalog:\n");
                individualCatalog(conn);
                break;
        }
    }
}


public static int selectCustomerPaymentMethods(int userId, Connection conn, boolean isInd, boolean isFinanced)
{
        if(isInd)
        {
            if(!isFinanced)
            {
                String bankAccountSelectionQuery = "WITH payments AS (SELECT * from payment where user_id = ?) SELECT * from bank_account where pay_id in (SELECT pay_id from payments) and pay_id = ?";
                String creditCardSelectionQuery = "WITH payments AS (SELECT * from payment where user_id = ?) SELECT * from credit_card where pay_id in (SELECT pay_id from payments) and pay_id = ?";

                try(PreparedStatement creditInformation = conn.prepareStatement(creditCardSelectionQuery);
                    PreparedStatement bankInformation = conn.prepareStatement(bankAccountSelectionQuery);)

                {
                    creditInformation.setInt(1, userId);
                    bankInformation.setInt(1, userId);
                    

                    int pay_id = getBankIdFromUser();
                    boolean isPresent = false;
                    boolean isCredit = false;

                    creditInformation.setInt(2, pay_id);
                    bankInformation.setInt(2, pay_id);

                    try(ResultSet rs = creditInformation.executeQuery())
                    {
                        if(rs.next())
                        {
                            isPresent = true;
                            return pay_id;
                        }
                    }
                    catch(SQLException e)
                    {
                        System.out.println("Error getting information about credit cards in sql" + e.getMessage());
                        System.exit(-1);
                    }

                    try(ResultSet rs = bankInformation.executeQuery())
                    {
                        if(rs.next())
                        {
                            isPresent = true;
                            return pay_id;
                        }
                    }
                    catch(SQLException e)
                    {
                        System.out.println("Error getting information about bank accounts in sql: " + e.getMessage());
                        System.exit(-1);
                    }

                    if(!isPresent)
                    {
                        System.out.println("Error: you do not own a bank account or credit card with that pay_id. Please try again.");
                        return -1;
                    }


                }
                catch(SQLException e)
                {
                    System.out.println("Unexpected SQL Error in getting Customer Payments: " + e.getMessage());
                    System.exit(-1);
                }
            }
            else
            {

            }
            
        }
        
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
                        System.out.println("\n\nError: You do not own that bank account. Please try Again");
                        return -1;
                    }
                    else
                    {
                        return bankId;
                    }
                }
                catch(SQLException e)
                {
                    System.out.println("Error getting information about bank accounts in sql: " + e.getMessage() );
                    System.exit(-1);
                }

            }
            catch(SQLException e)
            {
                System.out.println("Unexpected SQL Error in getting Customer Payments: " + e.getMessage());
                System.exit(-1);
            }            
        }
        return -1;
    }




    public static int getItemId(String prompt, Connection conn, String sql, String type)
    {
        System.out.print(prompt);
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
            System.out.println("Error Developing SQL prepared Statement, please try again 2: " + e.getMessage());
            return -1;
        }
    }

    public static void makeUnfinancedPurchase(Connection conn, int customerId)
    {
        individualCatalog(conn);
        String prompt = "Please enter the FULL NAME of an item above:";
        String sqlQ = "SELECT product_id from item where description = ?";
        int productId = getItemId(prompt, conn, sqlQ, "item");

        int paymentId = -1;
        
        while(paymentId < 0)
        {
            CustomerHelpers.getCustomerPaymentMethods(customerId, conn, true);
            paymentId = selectCustomerPaymentMethods(customerId, conn, true, false);
        }

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        


        String insertSql = "INSERT INTO Unfinanced_Transaction (date_of_delivery, settled_on, customer_id, pay_id, product_id) VALUES (?,?,?,?,?)";

        try(PreparedStatement insertionToTrans = conn.prepareStatement(insertSql);)
        {
            insertionToTrans.setDate(1, today);
            insertionToTrans.setDate(2,today);
            insertionToTrans.setInt(4, paymentId);
            insertionToTrans.setInt(5, productId);
            insertionToTrans.setInt(3, customerId);

            insertionToTrans.executeUpdate();
            System.out.println("  _______ _                 _     __     __         ");
            System.out.println(" |__   __| |               | |    \\ \\   / /         ");
            System.out.println("    | |  | |__   __ _ _ __ | | __  \\ \\_/ /__  _   _ ");
            System.out.println("    | |  | '_ \\ / _` | '_ \\| |/ /   \\   / _ \\| | | |");
            System.out.println("    | |  | | | | (_| | | | |   <     | | (_) | |_| |");
            System.out.println("    |_|  |_| |_|\\__,_|_| |_|_|\\_\\    |_|\\___/ \\__,_|");

            System.out.println("\n\nThank you for your purchase! What would you like to do next?\n");

        }
        catch(SQLException e)
        {
            System.out.println("Error Inserting transaction: " + e.getMessage());
        }
    }




    public static void makeFinancedPurchase(Connection conn, int customerId)
    {
        individualCatalog(conn);
        String prompt = "Please enter the FULL NAME of an item above:";


        String sqlQ = "SELECT product_id from item where description = ?";
        int productId = getItemId(prompt, conn, sqlQ, "item");

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

        prompt = "Please Enter the plan_id for the plan you would like to use:";
        String type = "Plan";
        String sql = "SELECT * from financing where plan_id = ?";
        
        int planId = -1; 
        while(planId < 0)
        {
            planId = getPlan(prompt, conn, sql, type);
        }
        
        
        String interestRateQ = "SELECT interest_rate/100 as ir from financing where plan_id = ?";
        String pricingQ = "SELECT price from item where product_id = ?";

        double price = 0;
        double interestRate = 0;
        double principal = 0;

        try(PreparedStatement irq = conn.prepareStatement(interestRateQ);
            PreparedStatement pq = conn.prepareStatement(pricingQ);)
        {
            irq.setInt(1, planId);
            pq.setInt(1, productId);
            try(ResultSet rs = irq.executeQuery())
            {
                if(rs.next())
                {
                    interestRate = rs.getDouble("ir");
                }
                else 
                {
                    System.out.println("Error: No matching interest rate");
                    System.exit(1);
                }
            }

            try(ResultSet rs = pq.executeQuery())
            {
                if(rs.next())
                {
                    price = rs.getDouble("price");
                }
                else 
                {
                    System.out.println("Error: No matching interest rate");
                    System.exit(1);
                }
            }

            principal = price*interestRate + price;



        }
        catch(SQLException e)
        {
            System.out.println("Error Developing SQL prepared Statement, please try again 1: " + e.getMessage());
            System.exit(-1);
        }
        
        java.sql.Date today2 = new java.sql.Date(System.currentTimeMillis());


        String insertSql = "INSERT INTO financed_Transaction VALUES (DEFAULT,?,?,?,?,?)";

        try(PreparedStatement insertionToTrans = conn.prepareStatement(insertSql);)
        {
            insertionToTrans.setDate(1, today2);
            insertionToTrans.setDouble(2, principal);
            insertionToTrans.setInt(3, customerId);
            insertionToTrans.setInt(4, productId);
            insertionToTrans.setInt(5, planId);

            insertionToTrans.executeUpdate();
            System.out.println("  _______ _                 _     __     __         ");
            System.out.println(" |__   __| |               | |    \\ \\   / /         ");
            System.out.println("    | |  | |__   __ _ _ __ | | __  \\ \\_/ /__  _   _ ");
            System.out.println("    | |  | '_ \\ / _` | '_ \\| |/ /   \\   / _ \\| | | |");
            System.out.println("    | |  | | | | (_| | | | |   <     | | (_) | |_| |");
            System.out.println("    |_|  |_| |_|\\__,_|_| |_|_|\\_\\    |_|\\___/ \\__,_|");

            System.out.println("\n\nThank you for your purchase! What would you like to do next?\n");

        }
        catch(SQLException e)
        {
            System.out.println("Error Inserting transaction: " + e.getMessage());
        }

    }




    public static int getPlan(String prompt, Connection conn, String sql, String type)
    {
        int planId = -1;
        showPlans(conn);
        System.out.print(prompt);
        String input = scan.nextLine().trim();
        try 
        {
            planId = Integer.parseInt(input);
            if (planId > 0) 
            {
            } 
            else 
            {
                System.out.println("Plan_id must be a positive integer.");
            }
        }
        catch(NumberFormatException e)
        {
            System.out.println("Please enter an integer.");
        }
        

        try(PreparedStatement pstat = conn.prepareStatement(sql);)
        {
            pstat.setInt(1, planId);
            try(ResultSet rs = pstat.executeQuery())
            {
                if(rs.next())
                {
                    return planId;
                }
                else 
                {
                    System.out.println("Error: No matching " + type + " found");
                    return -1;
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error Developing SQL prepared Statement, please try again 4: " + e.getMessage());
                return -1;
            }

        }
        catch(SQLException e)
        {
            System.out.println("Error Developing SQL prepared Statement, please try again 3: " + e.getMessage());
            return -1;
        }
    }

    public static void showPlans(Connection conn)
    {
        String planQuery = "SELECT * from financing";

        try(PreparedStatement planQueryStmt = conn.prepareStatement(planQuery);)
            {
                try(ResultSet rs = planQueryStmt.executeQuery())
                {
                    System.out.println("\n\n------------------------------Financing Plans------------------------------\n");
                    if(!rs.next())
                    {
                        System.out.println("There are no financing plans currently avaliable");
                    }
                    else
                    {
                        System.out.println(String.format("%-10s %-40s %-10s %-15s","Plan_id", "Contract", "Installments", "Interest Rates"));
                        do
                        {
                            int plan_id = rs.getInt("Plan_id");
                            String contract = rs.getString("Contract");
                            int installments = rs.getInt("Number_installments");
                            double interest_rate = rs.getDouble("interest_rate");
                            

                            System.out.println(String.format("%-10d %-40s %-10d %-12.2f", plan_id, contract, installments, interest_rate));
                        } while(rs.next());
                    }

                }   
                catch(SQLException e)
                {
                    System.out.println("Error Querying for financing plans");
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error Querying for financing plans");
            }
    }

    public static void addItem(Connection conn)
    {
        int itemSelection = 0;
        String[] catalogOptions = {
                "Add Service",
                "Add Item",
                "Go Back"
            };

        StringBuilder prompt = Main.chooseFromOptions("\nPlease enter a number to choose one of the following options", catalogOptions);
        
        int entry = -1;
        while(entry != 3)
        {
            entry = Main.getIntInRange(prompt,1,3);
            switch(entry)
            {
                case 1:
                    addService(conn);
                    break;
                case 2:
                    addProduct(conn);
                    break;
                }
 
        }

    }

    public static void addService(Connection conn)
    {

        String description;
        while (true)
        {
            System.out.print("Please Enter the name of the service: ");
            description = scan.nextLine().trim();
            if (!description.isEmpty()) {
                break;
            }
            System.out.println("Description cannot be empty.");
        }

        double price = -1;
        while (price <= 0) 
        {
            System.out.print("Please Enter the price of your service: ");
            String priceInput = scan.nextLine().trim();
            try 
            {
                price = Double.parseDouble(priceInput);
                if (price <= 0) 
                {
                    System.out.println("Price must be a positive number.");
                }
            } 
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number for price.");
            }
        }

        String vendor;
        while (true)
        {
            System.out.print("Please enter a vendor name: ");
            vendor = scan.nextLine().trim();
            if (!vendor.isEmpty())
            {
                break;
            }
            System.out.println("Vendor name cannot be empty.");
        }

        double duration = -1;
        while (duration <= 0) 
        {
            System.out.print("Please Enter duration (in hours (if minutes enter as fraction of hour)): ");
            String durationInput = scan.nextLine().trim();
            try 
            {
                duration = Double.parseDouble(durationInput);
                if (duration <= 0) 
                {
                    System.out.println("Duration must be a positive number.");
                }
            } 
            catch (NumberFormatException e) 
            {
                System.out.println("Invalid input. Please enter a valid number for duration.");
            }
        }


        String insertSql = "INSERT INTO Service (DESCRIPTION, PRICE, VENDOR, DURATION) VALUES (?,?,?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) 
        {
            pstmt.setString(1, description);
            pstmt.setDouble(2, price);
            pstmt.setString(3, vendor);
            pstmt.setDouble(4, duration);

            pstmt.executeUpdate();
            
            System.out.println("\n   _____                              ");
            System.out.println("  / ____|                             ");
            System.out.println(" | (___  _   _  ___ ___ ___  ___ ___  ");
            System.out.println("  \\___ \\| | | |/ __/ __/ _ \\/ __/ __| ");
            System.out.println("  ____) | |_| | (_| (_|  __/\\__ \\__ \\ ");
            System.out.println(" |_____/ \\__,_|\\___\\___\\___||___/___/ ");
            
            System.out.println("\n\nService added successfully!\n");

        } 
        catch (SQLException e) 
        {
            System.out.println("Error adding service: " + e.getMessage());
        }
    }




    public static void addProduct(Connection conn)
    {

        String description;
        while (true)
        {
            System.out.print("Please Enter the name of the item: ");
            description = scan.nextLine().trim();
            if (!description.isEmpty()) 
            {
                break;
            }
            System.out.println("Name cannot be empty.");
        }

        double price = -1;
        while (price <= 0) 
        {
            System.out.print("Please Enter the price of your item: ");
            String priceInput = scan.nextLine().trim();
            try 
            {
                price = Double.parseDouble(priceInput);
                if (price <= 0) 
                {
                    System.out.println("Price must be a positive number.");
                }
            } 
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number for price.");
            }
        }

        String vendor;
        while (true)
        {
            System.out.print("Please enter a vendor name: ");
            vendor = scan.nextLine().trim();
            if (!vendor.isEmpty())
            {
                break;
            }
            System.out.println("Vendor name cannot be empty.");
        }

        double duration = -1;
        while (duration <= 0) 
        {
            System.out.print("Please Enter Warranty Length in months: ");
            String durationInput = scan.nextLine().trim();
            try 
            {
                duration = Double.parseDouble(durationInput);
                if (duration <= 0) 
                {
                    System.out.println("Warranty length must be a positive number.");
                }
            } 
            catch (NumberFormatException e) 
            {
                System.out.println("Invalid input. Please enter a valid number for warranty length.");
            }
        }


        String insertSql = "INSERT INTO Item (DESCRIPTION, PRICE, VENDOR, WARRANTY_LENGTH) VALUES (?,?,?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) 
        {
            pstmt.setString(1, description);
            pstmt.setDouble(2, price);
            pstmt.setString(3, vendor);
            pstmt.setDouble(4, duration);

            pstmt.executeUpdate();
            
            System.out.println("\n   _____                              ");
            System.out.println("  / ____|                             ");
            System.out.println(" | (___  _   _  ___ ___ ___  ___ ___  ");
            System.out.println("  \\___ \\| | | |/ __/ __/ _ \\/ __/ __| ");
            System.out.println("  ____) | |_| | (_| (_|  __/\\__ \\__ \\ ");
            System.out.println(" |_____/ \\__,_|\\___\\___\\___||___/___/ ");
            
            System.out.println("\n\nItem added successfully!\n");

        } 
        catch (SQLException e) 
        {
            System.out.println("Error adding Item: " + e.getMessage());
        }
    }



    public static void showManagerItems(Connection conn)
    {
        System.out.println();
        System.out.println("------------------Items Currently Avaliable for Purchase by Individuals------------------");
        individualCatalog(conn);

        System.out.println();
        System.out.println("------------------Services Currently Avaliable for Purchase by Businesses------------------");
        businessCatalog(conn);
    }

    public static void removeItem(Connection conn)
    {
        showManagerItems(conn);
        
        boolean itemRemoved = false;
        
        while (!itemRemoved) {
            System.out.print("\nPlease enter the FULL NAME of the item or service you would like to remove: ");
            String itemName = scan.nextLine().trim();
            
            if (itemName.isEmpty()) 
            {
                System.out.println("Item name cannot be empty. Please try again.");
                continue;
            }
            
            //checking if the item is present
            String checkItemSql = "SELECT product_id FROM item WHERE description = ?";
            String checkServiceSql = "SELECT product_id FROM service WHERE description = ?";
            
            try (PreparedStatement itemStmt = conn.prepareStatement(checkItemSql);
                 PreparedStatement serviceStmt = conn.prepareStatement(checkServiceSql)) 
            {
                
                itemStmt.setString(1, itemName);
                try (ResultSet rs = itemStmt.executeQuery()) 
                {
                    if (rs.next()) 
                    {
                        int productId = rs.getInt("product_id");
                        String deleteSql = "DELETE FROM item WHERE product_id = ?";
                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) 
                        {
                            deleteStmt.setInt(1, productId);
                            deleteStmt.executeUpdate();
                            
                            System.out.println("\n  _____                                   _  ");
                            System.out.println(" |  __ \\                                 | | ");
                            System.out.println(" | |__) |___ _ __ ___   _____   _____  __| | ");
                            System.out.println(" |  _  // _ \\ '_ ` _ \\ / _ \\ \\ / / _ \\/ _` | ");
                            System.out.println(" | | \\ \\  __/ | | | | | (_) \\ V /  __/ (_| | ");
                            System.out.println(" |_|  \\_\\___|_| |_| |_|\\___/ \\_/ \\___|\\__,_| ");
                            System.out.println("\nItem removed successfully!\n");
                            itemRemoved = true;
                            continue;
                        }
                    }
                }
                
                serviceStmt.setString(1, itemName);
                try (ResultSet rs = serviceStmt.executeQuery()) 
                {
                    if (rs.next()) 
                    {
                        int productId = rs.getInt("product_id");
     

                        String deleteSql = "DELETE FROM service WHERE product_id = ?";
                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                            deleteStmt.setInt(1, productId);
                            deleteStmt.executeUpdate();
                            
                            System.out.println("\n  _____                                   _  ");
                            System.out.println(" |  __ \\                                 | | ");
                            System.out.println(" | |__) |___ _ __ ___   _____   _____  __| | ");
                            System.out.println(" |  _  // _ \\ '_ ` _ \\ / _ \\ \\ / / _ \\/ _` | ");
                            System.out.println(" | | \\ \\  __/ | | | | | (_) \\ V /  __/ (_| | ");
                            System.out.println(" |_|  \\_\\___|_| |_| |_|\\___/ \\_/ \\___|\\__,_| ");
                            System.out.println("\nService removed successfully!\n");
                            itemRemoved = true;
                            continue;
                        }
                    }
                }
                
                System.out.println("\nError: No item or service found with that name. Please try again.");
                
            } 
            catch (SQLException e) 
            {
                System.out.println("Error removing item: " + e.getMessage());
            }
        }
    }


public static void makePaymentTowardsFinancing(Connection conn, int customerId)
{
    String financedTransQuery = "SELECT ft.transaction_id, i.description, i.price, ft.remaining_balance, ft.date_of_delivery, f.number_installments FROM financed_transaction ft JOIN item i ON ft.product_id = i.product_id JOIN financing f ON ft.plan_id = f.plan_id WHERE ft.customer_id = ? AND ft.remaining_balance > 0 ORDER BY ft.date_of_delivery";


    try (PreparedStatement pstmt = conn.prepareStatement(financedTransQuery))
    {
        pstmt.setInt(1, customerId);
        ResultSet rs = pstmt.executeQuery();
        
        System.out.println("\n------Here are your current open Financed Transactions------");
        System.out.printf("%-10s %-35s %-15s %-15s %-20s%n", "Trans ID", "Item", "Total Price", "Balance Owed", "Installments");
        
   
        boolean hasBalance = false;
        while (rs.next())
        {
            hasBalance = true;
            int transId = rs.getInt("transaction_id");
            String description = rs.getString("description");
            double price = rs.getDouble("price");
            double balance = rs.getDouble("remaining_balance");
            int numInstallments = rs.getInt("number_installments");
            
            System.out.printf("%-10d %-35s $%-14.2f $%-14.2f %-20d%n", transId, description, price, balance, numInstallments);
        }
        
        if (!hasBalance)
        {
            System.out.println("You have no outstanding financed purchases. Batman would be impressed!");
            return;
        }

        System.out.println();
    }
    catch (SQLException e)
    {
        System.out.println("Error retrieving financed transactions: " + e.getMessage());
        return;
    }

    int transId = -1;
    double totalPrice = 0;
    int numInstallments2 = 0;
    double remainingBalance = 0;

    while(transId < 0)
    {
        System.out.print("Enter the Transaction ID you would like to make a payment toward:");

        String checkQuery = "SELECT i.price, f.number_installments, ft.remaining_balance FROM financed_transaction ft JOIN item i ON ft.product_id = i.product_id JOIN financing f ON ft.plan_id = f.plan_id WHERE ft.transaction_id = ? AND ft.customer_id = ? AND ft.remaining_balance > 0";
            
        String input = scan.nextLine().trim();

        try
        {
            transId = Integer.parseInt(input);
            if(transId < 0)
            {
                System.out.println("Error id cannot be negative");
                continue;
            }
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery))
            {
                checkStmt.setInt(1, transId);
                checkStmt.setInt(2, customerId);
                ResultSet verifyRs = checkStmt.executeQuery();
                
                if (!verifyRs.next())
                {
                    System.out.println("Invalid transaction ID or no balance remaining. Please try again.");
                    transId = -1;
                }
                else
                {
                    totalPrice = verifyRs.getDouble("price");
                    numInstallments2 = verifyRs.getInt("number_installments");
                    remainingBalance = verifyRs.getDouble("remaining_balance");
                }
            }
        }
        catch (NumberFormatException e)
        {
            System.out.println("Invalid input. Please enter a valid transaction ID.");
            transId = -1;
        }
        catch (SQLException e)
        {
            System.out.println("Error verifying transaction: " + e.getMessage());
            transId = -1;
        }
    }

    double standardInstallment = totalPrice / numInstallments2;
    int paymentsMadeCount = 0;

    try
    {
        String paymentsCountQuery  = "SELECT COUNT(*) as count from payment_made_toward WHERE transaction_id = ?";

        try(PreparedStatement countStmt = conn.prepareStatement(paymentsCountQuery))
        {
            countStmt.setInt(1, transId);
            ResultSet countRs = countStmt.executeQuery();
            if(countRs.next())
            {
                paymentsMadeCount = countRs.getInt("count");
            }
        }
    }
    catch(SQLException e)
    {
        System.out.println("Error counting payments: " + e.getMessage());
        return;
    }

    int paymentsRemaining = numInstallments2 - paymentsMadeCount;


    double paymentAmount;

    if(paymentsRemaining == 1)
    {
        paymentAmount = remainingBalance;
    }
    else if(paymentsMadeCount == 0)
    {
        double futurePaymentsTotal = standardInstallment * (numInstallments2 - 1);
        paymentAmount = totalPrice - futurePaymentsTotal;
    }
    else
    {

        paymentAmount = standardInstallment;
        if(paymentAmount > remainingBalance)
        {
            paymentAmount = remainingBalance;
        }
        
    }

    System.out.println("\n\nPayment Summary:");
    System.out.printf("Total Price of Item: $%.2f%n", totalPrice);
    System.out.printf("Current Balance: $%.2f%n", remainingBalance);
    System.out.printf("This Payment Amount: $%.2f%n", paymentAmount);
    System.out.printf("Balance After Payment: $%.2f%n", remainingBalance - paymentAmount);

    System.out.println();

    int paymentId = -1;

    while(paymentId < 0)
    {
        CustomerHelpers.getCustomerPaymentMethods(customerId, conn, true);
        paymentId = CatalogInterface.selectCustomerPaymentMethods(customerId, conn, true, false);
    }


    try
    {
        conn.setAutoCommit(false);

        String insertPaymentQuery = "Insert into transaction_for VALUES (?, ?, ?, ?, ?)";
        String insertPaymentMadeToward = "Insert into payment_made_toward values (?, ?, ?, ?, ?)";

        try(PreparedStatement insertStmt = conn.prepareStatement(insertPaymentQuery);
            PreparedStatement insertStmt2 = conn.prepareStatement(insertPaymentMadeToward);
        )
        {
            insertStmt.setInt(1, transId);
            insertStmt2.setInt(2, transId);

            insertStmt.setInt(2, paymentsMadeCount  + 1);
            insertStmt2.setInt(3, paymentsMadeCount + 1);

            insertStmt.setDouble(3, paymentAmount);
            insertStmt2.setDouble(4, paymentAmount);

            insertStmt.setInt(4, paymentsMadeCount + 1);
            insertStmt2.setInt(5, paymentsMadeCount + 1);

            insertStmt.setInt(5, paymentId);
            insertStmt2.setInt(1, paymentId);

            insertStmt.executeUpdate();
            insertStmt2.executeUpdate();
            conn.commit();

            double newBalance = remainingBalance - paymentAmount;

            System.out.println("  _______ _                 _     __     __         ");
            System.out.println(" |__   __| |               | |    \\ \\   / /         ");
            System.out.println("    | |  | |__   __ _ _ __ | | __  \\ \\_/ /__  _   _ ");
            System.out.println("    | |  | '_ \\ / _` | '_ \\| |/ /   \\   / _ \\| | | |");
            System.out.println("    | |  | | | | (_| | | | |   <     | | (_) | |_| |");
            System.out.println("    |_|  |_| |_|\\__,_|_| |_|_|\\_\\    |_|\\___/ \\__,_|");

            System.out.printf("\n\nThank you for your payment! Your remaining balance is %.2f. What would you like to do next?\n", newBalance);


        }
        catch (SQLException e)
        {
            conn.rollback();
            System.out.println("Error processing payment: " + e.getMessage());
        }
        finally
        {
            conn.setAutoCommit(true);
        }
    }
    catch (SQLException e)
    {
        System.out.println("Error managing transaction: " + e.getMessage());
    }
    

}
}

