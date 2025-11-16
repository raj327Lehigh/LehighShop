import java.sql.*;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
            "View Financing Plans",
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
                    reports(conn);
                    break;
                case 2:
                    addFinancingOption(conn);
                    break;
                case 3:
                    viewFinancingPlans(conn);
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
            "Highest Selling Items Report",
            "Outstanding Financing Report",
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
                    leaderBoardBuyers(conn);
                    break;
                case 2:
                    MostSoldItems(conn);
                    break;
                case 3:
                    outstandingFinancingReport(conn);
                    break;
            }
        }
    }

    //LeaderBoard for most spending
    //Most Sold Items
    //Outstanding Financing Report 

    public static void leaderBoardBuyers(Connection conn)
    {
        System.out.println("\n\n------------------------------------------Manager's Report: Leaderboard Buyers------------------------------------------");
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = today.format(formatter);
        System.out.println();
        System.out.println("The report below identifies the highest spenders at LUshop as of: " + formattedDate);

        System.out.println();
        
        System.out.println("\n\n-----------------------Highest Spending Business Customers-----------------------");

        String businessSpendingQuery = "WITH allBusinessTransactions AS (SELECT bt.customer_id, s.price FROM bus_transaction bt JOIN service s USING (product_id) JOIN business b ON bt.customer_id = b.customer_id), businessSpending AS (SELECT customer_id, SUM(price) as total_spent FROM allBusinessTransactions GROUP BY customer_id) SELECT RANK() OVER (ORDER BY total_spent DESC) as ranking, customer_id, c.name, bsp.total_spent FROM businessSpending bsp JOIN customer c USING (customer_id) ORDER BY ranking";


        try(PreparedStatement businessSpending = conn.prepareStatement(businessSpendingQuery);
            ResultSet rs = businessSpending.executeQuery();)
        {
            System.out.printf("%-10s %-12s %-35s %-15s%n", "Ranking", "Customer ID", "Name", "Total Spent");
            while(rs.next())
            {
                //SELECT RANK() OVER (ORDER BY total_spent DESC) as ranking, customer_id, c.name, bsp.total_spent FROM businessSpending bsp JOIN customer c USING (customer_id) ORDER BY ranking
                int ranking = rs.getInt("ranking");
                int customer_id = rs.getInt("customer_id");
                String name = rs.getString("name");
                double amount_spent = rs.getDouble("total_spent");
                
                System.out.printf("%-10d %-12d %-35s $%-14.2f%n", ranking, customer_id, name, amount_spent);
            }

            if(rs.isBeforeFirst())
            {
                System.out.println("No individual customer purchases found.");
            }
            
        }
        catch(SQLException e)
        {
            System.out.println("Error generating individual spending report: " + e.getMessage());
        }

        System.out.println();
        System.out.println();



        System.out.println("\n\n-----------------------Highest Spending Individual Customers-----------------------");

        String individualSpendingQuery = "WITH allIndividualTransactions AS (SELECT ft.customer_id, i.price FROM financed_transaction ft JOIN item i USING (product_id) JOIN individual ind ON ft.customer_id = ind.customer_id UNION ALL SELECT ut.customer_id, i.price FROM unfinanced_transaction ut JOIN item i USING (product_id) JOIN individual ind ON ut.customer_id = ind.customer_id), individualSpending AS (SELECT customer_id, SUM(price) as total_spent FROM allIndividualTransactions GROUP BY customer_id) SELECT RANK() OVER (ORDER BY total_spent DESC) as ranking, customer_id, c.name, isp.total_spent FROM individualSpending isp JOIN customer c USING (customer_id) ORDER BY ranking";


        try(PreparedStatement individualSpending = conn.prepareStatement(individualSpendingQuery);
            ResultSet rs = individualSpending.executeQuery();)
        {
            System.out.printf("%-10s %-12s %-35s %-15s%n", "Ranking", "Customer ID", "Name", "Total Spent");
            while(rs.next())
            {
                //SELECT RANK() OVER (ORDER BY total_spent DESC) as ranking, customer_id, c.name, bsp.total_spent FROM businessSpending bsp JOIN customer c USING (customer_id) ORDER BY ranking
                int ranking = rs.getInt("ranking");
                int customer_id = rs.getInt("customer_id");
                String name = rs.getString("name");
                double amount_spent = rs.getDouble("total_spent");
                
                System.out.printf("%-10d %-12d %-35s $%-14.2f%n", ranking, customer_id, name, amount_spent);
            }

            if(rs.isBeforeFirst())
            {
                System.out.println("No individual customer purchases found.");
            }
            
        }
        catch(SQLException e)
        {
            System.out.println("Error generating individual spending report: " + e.getMessage());
        }

        System.out.println();
        System.out.println();




        System.out.println("\n\n-----------------------Highest Spending All Customers-----------------------");

        String allCustomerSpendingQuery = "WITH allCustomerTransactions AS (SELECT ft.customer_id, i.price FROM financed_transaction ft JOIN item i USING (product_id) UNION ALL SELECT ut.customer_id, i.price FROM unfinanced_transaction ut JOIN item i USING (product_id) UNION ALL SELECT bt.customer_id, s.price FROM bus_transaction bt JOIN service s USING (product_id)), customerSpending AS (SELECT customer_id, SUM(price) as total_spent FROM allCustomerTransactions GROUP BY customer_id) SELECT RANK() OVER (ORDER BY total_spent DESC) as ranking, customer_id, c.name, cs.total_spent FROM customerSpending cs JOIN customer c USING (customer_id) ORDER BY ranking";


        try(PreparedStatement allCustomerSpending = conn.prepareStatement(allCustomerSpendingQuery);
            ResultSet rs = allCustomerSpending.executeQuery();)
        {
            System.out.printf("%-10s %-12s %-35s %-15s%n", "Ranking", "Customer ID", "Name", "Total Spent");
            while(rs.next())
            {
                //SELECT RANK() OVER (ORDER BY total_spent DESC) as ranking, customer_id, c.name, bsp.total_spent FROM businessSpending bsp JOIN customer c USING (customer_id) ORDER BY ranking
                int ranking = rs.getInt("ranking");
                int customer_id = rs.getInt("customer_id");
                String name = rs.getString("name");
                double amount_spent = rs.getDouble("total_spent");
                
                System.out.printf("%-10d %-12d %-35s $%-14.2f%n", ranking, customer_id, name, amount_spent);
            }

            if(rs.isBeforeFirst())
            {
                System.out.println("No individual customer purchases found.");
            }
            
        }
        catch(SQLException e)
        {
            System.out.println("Error generating individual spending report: " + e.getMessage());
        }

        System.out.println();
        System.out.println();

        System.out.println("--------------------Report Generated Above--------------------\n");
        System.out.println("What would you like to do next?");
    }




    public static void MostSoldItems(Connection conn)
    {
        System.out.println("\n\n------------------------------------------Manager's Report: Best Selling Products------------------------------------------");
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = today.format(formatter);
        System.out.println();
        System.out.println("The report below identifies the highest selling products at LUshop as of: " + formattedDate);

        System.out.println();
        
        System.out.println("\n\n----------------------------------------------Best Selling Services----------------------------------------------");

        String serviceSalesQuery = "WITH serviceCount AS (SELECT product_id, count(*) AS sales FROM bus_transaction GROUP BY product_id) SELECT rank() over(order by sales*price desc) as ranking, product_id, description, vendor, sales, price, sales*price as Total_Value FROM service JOIN serviceCount USING (product_id) order by ranking";

        try(PreparedStatement serviceSales = conn.prepareStatement(serviceSalesQuery);
            ResultSet rs = serviceSales.executeQuery();)
        {
            System.out.printf("%-10s %-12s %-35s %-25s %-10s %-12s %-15s%n", "Ranking", "Product ID", "Description", "Vendor", "Sales", "Price", "Total Value");
            
            while(rs.next())
            {
                int ranking = rs.getInt("ranking");
                int product_id = rs.getInt("product_id");
                String description = rs.getString("description");
                String vendor = rs.getString("vendor");
                int sales = rs.getInt("sales");
                double price = rs.getDouble("price");
                double total_value = rs.getDouble("Total_Value");
                
                System.out.printf("%-10d %-12d %-35s %-25s %-10d $%-11.2f $%-14.2f%n", ranking, product_id, description, vendor, sales, price, total_value);
            }
            
            if(rs.isBeforeFirst())
            {
                System.out.println("No service sales found.");
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error generating service sales report: " + e.getMessage());
        }

        
        System.out.println("\n\n----------------------------------------------Best Selling Items (Unfinanced)----------------------------------------------");

        String unfinancedItemsQuery = "WITH unFinancedCount as (SELECT product_id, count(*) as sales from UNFINANCED_TRANSACTION group by product_id) SELECT rank() over(order by sales*price desc) as ranking, product_id, description, vendor, sales, price, sales*price as total_value from ITEM join unFinancedCount using (product_id) order by ranking";
        try(PreparedStatement unfinancedItems= conn.prepareStatement(unfinancedItemsQuery);
            ResultSet rs = unfinancedItems.executeQuery();)
        {
            System.out.printf("%-10s %-12s %-35s %-25s %-10s %-12s %-15s%n", "Ranking", "Product ID", "Description", "Vendor", "Sales", "Price", "Total Value");
            
            while(rs.next())
            {
                int ranking = rs.getInt("ranking");
                int product_id = rs.getInt("product_id");
                String description = rs.getString("description");
                String vendor = rs.getString("vendor");
                int sales = rs.getInt("sales");
                double price = rs.getDouble("price");
                double total_value = rs.getDouble("Total_Value");
                
                System.out.printf("%-10d %-12d %-35s %-25s %-10d $%-11.2f $%-14.2f%n", ranking, product_id, description, vendor, sales, price, total_value);
            }
            
            if(rs.isBeforeFirst())
            {
                System.out.println("No unfinanced items found.");
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error generating unfinanced sales report: " + e.getMessage());
        }


        System.out.println("\n\n----------------------------------------------Best Selling Items (Financed)----------------------------------------------");

        String financedItemsQuery = "WITH financedCount as (SELECT product_id, count(*) as sales from FINANCED_TRANSACTION group by product_id) SELECT rank() over(order by sales*price desc) as ranking, product_id, description, vendor, sales, price, sales*price as total_value from ITEM join financedCount using (product_id) order by ranking";
        try(PreparedStatement financedItems= conn.prepareStatement(financedItemsQuery);
            ResultSet rs = financedItems.executeQuery();)
        {
            System.out.printf("%-10s %-12s %-35s %-25s %-10s %-12s %-15s%n", "Ranking", "Product ID", "Description", "Vendor", "Sales", "Price", "Total Value");
            
            while(rs.next())
            {
                int ranking = rs.getInt("ranking");
                int product_id = rs.getInt("product_id");
                String description = rs.getString("description");
                String vendor = rs.getString("vendor");
                int sales = rs.getInt("sales");
                double price = rs.getDouble("price");
                double total_value = rs.getDouble("Total_Value");
                
                System.out.printf("%-10d %-12d %-35s %-25s %-10d $%-11.2f $%-14.2f%n", ranking, product_id, description, vendor, sales, price, total_value);
            }
            
            if(rs.isBeforeFirst())
            {
                System.out.println("No unfinanced items found.");
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error generating financed sales report: " + e.getMessage());
        }


        System.out.println("\n\n----------------------------------------------Best Selling Items (Regardless of Financing)----------------------------------------------");

        String allItemTransactionsQuery = "WITH allItemTransactions AS (SELECT product_id FROM financed_transaction UNION ALL SELECT product_id FROM unfinanced_transaction), transactionCount AS (SELECT product_id, COUNT(*) as sales FROM allItemTransactions GROUP BY product_id) SELECT rank() OVER(ORDER BY sales*price DESC) as ranking, product_id, description, vendor, sales, price, sales * price AS total_value FROM item JOIN transactionCount USING (product_id) ORDER BY ranking";        
        
        try(PreparedStatement allItems= conn.prepareStatement(allItemTransactionsQuery);
            ResultSet rs = allItems.executeQuery();)
        {
            System.out.printf("%-10s %-12s %-35s %-25s %-10s %-12s %-15s%n", "Ranking", "Product ID", "Description", "Vendor", "Sales", "Price", "Total Value");
            
            while(rs.next())
            {
                int ranking = rs.getInt("ranking");
                int product_id = rs.getInt("product_id");
                String description = rs.getString("description");
                String vendor = rs.getString("vendor");
                int sales = rs.getInt("sales");
                double price = rs.getDouble("price");
                double total_value = rs.getDouble("total_value");
                
                System.out.printf("%-10d %-12d %-35s %-25s %-10d $%-11.2f $%-14.2f%n", ranking, product_id, description, vendor, sales, price, total_value);
            }
            
            if(rs.isBeforeFirst())
            {
                System.out.println("No items found.");
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error generating all items sales report: " + e.getMessage());
        }


        System.out.println("\n\n----------------------------------------------Best Selling Products (All Items + Services)----------------------------------------------");

        String allTransactionsQuery = "WITH allTransactions AS (SELECT product_id FROM financed_transaction UNION ALL SELECT product_id FROM unfinanced_transaction UNION ALL SELECT product_id FROM BUS_TRANSACTION), transactionCount AS (SELECT product_id, COUNT(*) as sales FROM allTransactions GROUP BY product_id), purchaseable AS (SELECT product_id, description, price, vendor, duration, NULL as warranty_length FROM service UNION ALL SELECT product_id, description, price, vendor, NULL as duration, warranty_length FROM item) SELECT rank() OVER (ORDER BY sales*price DESC) as ranking, product_id, description, vendor, sales, price, sales * price AS total_value FROM purchaseable JOIN transactionCount USING (product_id) order by ranking";

        try(PreparedStatement allProducts = conn.prepareStatement(allTransactionsQuery);
            ResultSet rs = allProducts.executeQuery();)
        {
            System.out.printf("%-10s %-12s %-35s %-25s %-10s %-12s %-15s%n", "Ranking", "Product ID", "Description", "Vendor", "Sales", "Price", "Total Value");
            
            while(rs.next())
            {
                int ranking = rs.getInt("ranking");
                int product_id = rs.getInt("product_id");
                String description = rs.getString("description");
                String vendor = rs.getString("vendor");
                int sales = rs.getInt("sales");
                double price = rs.getDouble("price");
                double total_value = rs.getDouble("total_value");
                
                System.out.printf("%-10d %-12d %-35s %-25s %-10d $%-11.2f $%-14.2f%n", ranking, product_id, description, vendor, sales, price, total_value);
            }
            
            if(rs.isBeforeFirst())
            {
                System.out.println("No products found.");
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error generating all products sales report: " + e.getMessage());
        }

        System.out.println();
        System.out.println();


        System.out.println("--------------------Report Generated Above--------------------\n");
        System.out.println("What would you like to do next?");
    }




    public static void outstandingFinancingReport(Connection conn)
    {
        System.out.println("\n\n------------------------------------------Manager's Report: Outstanding Financing Report ------------------------------------------");
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = today.format(formatter);
        System.out.println();
        System.out.println("The report below identifies the financing situation at LUshop as of: " + formattedDate);

        System.out.println();
        
        System.out.println("\n\n----------------------------------------------Customers With Highest Debt----------------------------------------------");

        String moneyDueRankingQuery = "SELECT rank() over(order by sum(REMAINING_BALANCE) desc) as money_due_rank, name, sum(REMAINING_BALANCE) as Money_DUE, count(*) as Financed_Purchases from financed_transaction join customer using (customer_id) group by name order by money_due_rank";
        String percentageFinancedQuery = "WITH ft as (SELECT count(*) as cnt from financed_transaction), ut as (SELECT count(*) as cnt from UNFINANCED_TRANSACTION), bt as (SELECT count(*) as cnt from BUS_TRANSACTION) SELECT round((SELECT cnt from ft) / ((SELECT cnt from ut) + (SELECT cnt from bt) + (SELECT cnt from ft)), 2) as percentageFinanced from ft, ut, bt";
        
        try(PreparedStatement moneyDueStatement = conn.prepareStatement(moneyDueRankingQuery);
            PreparedStatement percentageFinancedQueryR = conn.prepareStatement(percentageFinancedQuery);
            ResultSet rs = moneyDueStatement.executeQuery();
            ResultSet rs2 = percentageFinancedQueryR.executeQuery();)
        {
            System.out.printf("\n\n%-10s %-35s %-25s %-25s%n", "Ranking", "Name", "Outstanding Balance", "Financed Purchases");
        
            double totalOutstanding = 0;
        
            while(rs.next())
            {
                int ranking = rs.getInt("money_due_rank");
                String name = rs.getString("name");
                double money_due = rs.getDouble("Money_DUE");
                int financed_purchases = rs.getInt("Financed_Purchases");
                totalOutstanding += money_due;
                
                System.out.printf("%-10d %-35s $%-24.2f %25d %n", ranking, name, money_due, financed_purchases);
            }

            if(rs.isBeforeFirst())
            {
                System.out.println("No customers with outstanding financing found.");
            }
            else
            {
                System.out.println("-------------------------------------------------------------------------------------------------------");
                System.out.printf("%-10s %-35s $%-24.2f %-25s%n", "", "Total Outstanding", totalOutstanding, "");
            }

            if(rs2.next())
            {
                double percentageOfItemsSoldFinanced = rs2.getDouble("percentageFinanced");

                System.out.println("-------------------------------------------------------------------------------------------------------");
                String percentage = String.format("%.2f%%", percentageOfItemsSoldFinanced * 100);
                System.out.printf("%-10s %-35s %-27s %-25s%n", "", "Percentage of Sales Financed", percentage, "");
            }
            else
            {
                System.out.println("Unable to calculate percentage of sales financed.");
            }
                
        }
        catch(SQLException e)
        {
            System.out.println("Error generating financing report: " + e.getMessage());
        }

        System.out.println();
        System.out.println();

        System.out.println("--------------------Report Generated Above--------------------\n");
        System.out.println("What would you like to do next?");
    }





    public static void addFinancingOption(Connection conn)
    {
        
        String name;
        while (true)
        {
            System.out.print("Enter the name of the financing plan (typically of format X-Month (type of plan) Plan) : ");
            name = scan.nextLine().trim();
            if (!name.isEmpty()) {
                break;
            }
            System.out.println("Name cannot be empty.");
        }

        int numInstallement = -1;

        while(numInstallement <= 0)
        {
            System.out.print("Enter the number of installments you want on this plan: ");
            String readIn = scan.nextLine().trim();

            try
            {
                numInstallement = Integer.parseInt(readIn);
            }
            catch(NumberFormatException e)
            {
                System.out.println("The number of installments must be an integer. Please try again.");
                continue;
            }

            if (numInstallement <= 0)
            {
                System.out.println("Number of installments must be a positive number. Please try again.");
            }
        }

        double interestRate = -1;

        while(interestRate <= 0)
        {
            System.out.print("Enter the interest rate you would like on the plan (as integer (20 would be 20%)): ");

            String readIn = scan.nextLine().trim();

            try
            {
                interestRate = Double.parseDouble(readIn);
            }
            catch(NumberFormatException e)
            {
                System.out.println("The interest rate must be an number. Please try again.");
                continue;
            }

            if (interestRate <= 0 || interestRate >= 100)
            {
                System.out.println("The interest rate must be between 0 and 100. Please try again.");
            }
        }

        String insertSql = "INSERT INTO financing (contract, number_installments, interest_rate) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) 
        {
            pstmt.setString(1, name);
            pstmt.setInt(2, numInstallement);
            pstmt.setDouble(3, interestRate);
            pstmt.executeUpdate();
            System.out.println("\n   _____                              ");
            System.out.println("  / ____|                             ");
            System.out.println(" | (___  _   _  ___ ___ ___  ___ ___  ");
            System.out.println("  \\___ \\| | | |/ __/ __/ _ \\/ __/ __| ");
            System.out.println("  ____) | |_| | (_| (_|  __/\\__ \\__ \\ ");
            System.out.println(" |_____/ \\__,_|\\___\\___\\___||___/___/ ");
            System.out.println();
            System.out.println("Financing plan added successfully!");
            System.out.println("What would you like to do next?");
            System.out.println();
        } 
        catch (SQLException e) {
            System.out.println("Error adding financing plan: " + e.getMessage());
        }

        
    }

    public static void viewFinancingPlans(Connection conn)
    {
        System.out.println("\n\n------------------------------------------Available Financing Plans------------------------------------------");
        
        String query = "SELECT plan_id, contract, number_installments, interest_rate FROM financing ORDER BY number_installments";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery())
        {
            System.out.printf("\n%-10s %-50s %-20s %-15s%n", "Plan ID", "Plan Name", "Installments", "Interest Rate");            
            boolean hasPlans = false;
            while (rs.next())
            {
                hasPlans = true;
                int planId = rs.getInt("plan_id");
                String contract = rs.getString("contract");
                int numInstallments = rs.getInt("number_installments");
                double interestRate = rs.getDouble("interest_rate");
                
                System.out.printf("%-10d %-50s %-20d %-15.2f%%%n", planId, contract, numInstallments, interestRate);
            }
            
            if (!hasPlans)
            {
                System.out.println("No financing plans available.");
            }
            
            System.out.println("What would you like to do next?\n");
        }
        catch (SQLException e)
        {
            System.out.println("Error retrieving financing plans: " + e.getMessage());
        }
    }
}



