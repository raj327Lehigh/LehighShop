import java.sql.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;


public class CustomerHelpers{
    private static final Scanner scan = new Scanner(System.in);
    public static void currentUserList(Connection conn, boolean isIndividual)
    {
        if(isIndividual)
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

        }
        else
        {
            System.out.println("\n\nCurrent Business Customer names: ");

            String sql = "SELECT * FROM business JOIN customer USING (customer_id)";
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
        
        System.out.println();
    }


    public static void currentManagerList(Connection conn)
    {
        
        System.out.println();
        System.out.println("\n\nCurrent Manager names: ");

        String sql = "SELECT * FROM manager";
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
            System.out.println("Error fetching managers: " + e.getMessage());
        }
        System.out.println();
    }






    public static int signUserIn(Connection conn, int userBusinessManager)
    {
        int customerID = -1;

        if(userBusinessManager == 1)
        {
            while(customerID == -1)
            {
                String customerPrompt = "Please Enter the FULL NAME of the customer you would like to sign in as from the list above:";
                String sqlQ = "SELECT customer_id from customer where name = ?";
                customerID = signIn(customerPrompt, conn, sqlQ, "individual");
            }
        }
        else if(userBusinessManager == 2)
        {
            while(customerID == -1)
            {
                String businessPrompt = "Please Enter the FULL NAME of the business you would like to sign in as from the list above:";
                String sqlQ = "SELECT customer_id from customer where name = ?";
                customerID = signIn(businessPrompt, conn, sqlQ, "business");
            }
        }
        else if(userBusinessManager == 3)
        {
            while(customerID == -1)
            {
                String managerPrompt = "Please enter the FULL NAME of the manager you would like to sign in as from the list above:";
                String sqlQ = "SELECT manager_id from manager where name = ?";
                customerID = signInManager(managerPrompt, conn, sqlQ, "manager");
            }
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


    public static int signInManager(String prompt, Connection conn, String sql, String type)
    {
        System.out.print(prompt);    
        String userInput = scan.nextLine();
        try(PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, userInput);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) 
                {
                    int manager_id = rs.getInt("manager_id");
                    return manager_id;
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





    public static void getCustomerPaymentMethods(int userId,Connection conn, boolean isInd)
    {
        if(isInd)
        {

            System.out.println("\n------------------------Customer Payment Information------------------------");
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
                        System.out.println("\n------------------------Credit Cards on File------------------------");
                        System.out.println("No Credit Cards on File");
                    }
                    else
                    {
                        System.out.println("\n------------------------Credit Cards on File------------------------");
                        System.out.println(String.format("%-4s%-8s%-20s%-12s%-40s", "No.", "Pay ID", "Card Number", "Exp. Date", "Billing Address"));
                        int i = 1;
                        do {
                            int payId = rs.getInt("pay_id");
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

                            System.out.println(String.format("%-4s%-8d%-20s%-12s%-40s", i + ".", payId, cardNumber, formattedExpiry, formattedAddress));
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
                    System.out.println("\n------------------------Bank Accounts on File------------------------");
                    if(!rs.next())
                    {
                        System.out.println("No Bank Accounts on File\n");
                    }
                    else
                    {
                        System.out.println(String.format("%-4s%-8s%-20s%-20s%-20s", "No.", "Pay ID", "Account Number", "Bank", "Routing Number"));
                        int i = 1;
                        do {
                            int payId = rs.getInt("pay_id");
                            String accountNumber = rs.getString("account_number");
                            String bankName = rs.getString("bank");
                            String routingNumber = rs.getString("routing_number");
                            System.out.println(String.format("%-4s%-8d%-20s%-20s%-20s", i + ".", payId, accountNumber, bankName, routingNumber));
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
        else
        {
            System.out.println("\n------------------------Business Account Payment Information------------------------");

            String bankAccountQuery = "WITH payments AS (SELECT * from payment where user_id = ?) SELECT * from bank_account where pay_id in (SELECT pay_id from payments)";
            try(PreparedStatement bankInformation = conn.prepareStatement(bankAccountQuery);)
            {
                bankInformation.setInt(1, userId);

                try(ResultSet rs = bankInformation.executeQuery())
                {
                    System.out.println("\n------------------------Bank Accounts on File------------------------");
                    if(!rs.next())
                    {
                        System.out.println("No Bank Accounts on File\n");
                    }
                    else
                    {
                        System.out.println(String.format("%-4s%-8s%-20s%-20s%-20s", "No.", "Pay ID", "Account Number", "Bank", "Routing Number"));
                        int i = 1;
                        do {
                            int payId = rs.getInt("pay_id");
                            String accountNumber = rs.getString("account_number");
                            String bankName = rs.getString("bank");
                            String routingNumber = rs.getString("routing_number");
                            System.out.println(String.format("%-4s%-8d%-20s%-20s%-20s", i + ".", payId, accountNumber, bankName, routingNumber));
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
    


    public static void addCustomerBankAccount(int userId, Connection conn, boolean isInd) throws SQLException
    {
        try
        {

            String sql1 = "INSERT INTO Payment(user_id) VALUES (?)";
            try(PreparedStatement paymentInsert = conn.prepareStatement(sql1))
            {
                paymentInsert.setInt(1,userId);
                paymentInsert.executeUpdate();
            }
            catch(SQLException e)
            {
                throw e;
            }
            
            String sql2 = "SELECT MAX(pay_id) from payment where user_id = ?";
            int pay_id = -1;
            try(PreparedStatement payIdQuery = conn.prepareStatement(sql2))
            {
                payIdQuery.setInt(1, userId);
                try(ResultSet rs = payIdQuery.executeQuery())
                {
                    if(rs.next())
                    {
                        pay_id = rs.getInt(1);
                    }
                }
                catch(SQLException e)
                {
                    throw e;
                }
            }

            String sql3 = "INSERT INTO Bank_Account (pay_id, routing_number, account_number, bank) VALUES (?, ?, ?, ?)";
            String[] bankInfo = getBankAccountInput();

            try(PreparedStatement bankInsert = conn.prepareStatement(sql3))
            {
                bankInsert.setInt(1, pay_id);
                bankInsert.setInt(2, Integer.parseInt(bankInfo[2]));
                bankInsert.setInt(3, Integer.parseInt(bankInfo[0]));
                bankInsert.setString(4, bankInfo[1]);
                bankInsert.executeUpdate(); 
            }
            catch(SQLException e)
            {
                throw e;
            }
            System.out.println("\n\nBank account added successfully!\n\n");
        }
        catch(SQLException e)
        {
            throw e;
        }
        
    }

    public static String[] getBankAccountInput() 
    {
        String accountNumber;
        while (true) 
        {
            System.out.print("Please Enter the bank account number (digits only 6-20 digits): ");
            accountNumber = scan.nextLine().trim();
            if (accountNumber.matches("\\d{6,20}"))
            {
                break;
            }
            System.out.println("Invalid account number. Must be 6-20 digits.");
        }

        String bankName;
        String[] validBanks = {"Chase", "Wells Fargo", "Bank of America", "PNC"};
        while (true) 
        {
            System.out.print("Please Enter the bank name (Chase, Wells Fargo, Bank of America, PNC): ");
            bankName = scan.nextLine().trim();
            boolean valid = false;
            for (String realBank : validBanks) {
                if (bankName.equalsIgnoreCase(realBank)) 
                {
                    bankName = realBank; //make it the right capitilization 
                    valid = true;
                    break;
                }
            }
            if (valid) 
            {
                break;
            }
            System.out.println("Bank name must be one of: Chase, Wells Fargo, Bank of America, PNC.");
        }

        String routingNumber;
        while (true) 
        {
            System.out.print("Enter routing number (9 digits): ");
            routingNumber = scan.nextLine().trim();
            if (routingNumber.matches("\\d{9}")) 
            {
                break;
            }
            System.out.println("Invalid routing number. Must be 9 digits.");
        }

        return new String[] { accountNumber, bankName, routingNumber };
    }




    public static void seePurchaseHistory(int userId, Connection conn, boolean isInd)
    {
        if(isInd)
        {
            String financedItemsQuery =
            "WITH financedTransactions AS (" +
            "    SELECT * FROM financed_transaction WHERE customer_id = ?" +
            ") " +
            "SELECT * FROM item I JOIN financedTransactions F ON I.product_id = F.product_id";

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");

            try(PreparedStatement financedStatement = conn.prepareStatement(financedItemsQuery))
            {
                financedStatement.setInt(1, userId);
                try(ResultSet rs = financedStatement.executeQuery())
                {
                    System.out.println("\n\n------------------------------Financed Purchases------------------------------\n");
                    if(!rs.next())
                    {
                        System.out.println("No Financed Purchases have been made by this account");
                    }
                    else
                    {
                        System.out.println(String.format("%-30s %-18s %-10s %-15s %-18s","Description", "Vendor", "Price", "Delivery Date", "Remaining Balance"));
                        do
                        {
                            String description = rs.getString("DESCRIPTION");
                            String vendor = rs.getString("VENDOR");
                            double price = rs.getDouble("PRICE");
                            String deliveryDate = "";
                            try {
                                java.sql.Date deliverySqlDate = rs.getDate("DATE_OF_DELIVERY");
                                if (deliverySqlDate != null) 
                                {
                                    deliveryDate = dateFormat.format(deliverySqlDate);
                                }
                            } 
                            catch (Exception e)
                            {
                                deliveryDate = rs.getString("DATE_OF_DELIVERY"); 
                            }
                            double remainingBalance = rs.getDouble("REMAINING_BALANCE");

                            System.out.println(String.format("%-30s %-18s $%-9.2f %-15s $%-17.2f", description, vendor, price, deliveryDate, remainingBalance));
                        } while(rs.next());
                    }

                }   
                catch(SQLException e)
                {
                    System.out.println("Error Querying for financed transactions");
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error Querying for financed transactions");
            }


            String unfinancedItemsQuery =
            "WITH unfinancedTransactions AS (" +
            "    SELECT * FROM unfinanced_transaction WHERE customer_id = ?" +
            ") " +
            "SELECT * FROM item I JOIN unfinancedTransactions U ON I.product_id = U.product_id";

            try(PreparedStatement unfinancedStatement = conn.prepareStatement(unfinancedItemsQuery))
            {
                unfinancedStatement.setInt(1, userId);
                try(ResultSet rs = unfinancedStatement.executeQuery())
                {
                    System.out.println("\n\n------------------------------Unfinanced Purchases Made------------------------------\n");
                    if(!rs.next())
                    {
                        System.out.println("No Unfinanced Purchases have been made by this account");
                    }
                    else
                    {
                        System.out.println(String.format("%-30s %-18s %-10s %-15s","Description", "Vendor", "Price", "Delivery Date"));
                        do
                        {
                            String description = rs.getString("DESCRIPTION");
                            String vendor = rs.getString("VENDOR");
                            double price = rs.getDouble("PRICE");
                            String deliveryDate = "";
                            try {
                                java.sql.Date deliverySqlDate = rs.getDate("DATE_OF_DELIVERY");
                                if (deliverySqlDate != null) 
                                {
                                    deliveryDate = dateFormat.format(deliverySqlDate);
                                }
                            } 
                            catch (Exception e)
                            {
                                deliveryDate = rs.getString("DATE_OF_DELIVERY"); 
                            }

                            System.out.println(String.format("%-30s %-18s $%-9.2f %-15s", description, vendor, price, deliveryDate));
                        } while(rs.next());
                    }

                }   
                catch(SQLException e)
                {
                    System.out.println("Error Querying for unfinanced transactions");
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error Querying for unfinanced transactions");
            }


            String financingPaymentsQuery ="WITH payments AS ( SELECT pay_id FROM payment WHERE user_id = ?)SELECT  installment_id, payment_amount, description FROM transaction_for tf JOIN financed_transaction ft ON tf.transaction_id = ft.transaction_id JOIN item i ON i.product_id = ft.product_id WHERE tf.pay_id IN (SELECT pay_id FROM payments)";

            try(PreparedStatement payments_made = conn.prepareStatement(financingPaymentsQuery))
            {
                payments_made.setInt(1, userId);
                try(ResultSet rs = payments_made.executeQuery())
                {
                    System.out.println("\n\n------------------------------Financing Payments------------------------------\n");
                    if(!rs.next())
                    {
                        System.out.println("No Financing payments have been made with your payment methods");
                    }
                    else
                    {
                        System.out.println(String.format("%-10s %-30s %-20s","Payment #", "Item", "Amount" ));
                        do
                        {
                            String description = rs.getString("description");
                            int installment_id = rs.getInt("installment_id");
                            double payment_amount = rs.getDouble("payment_amount");

                            System.out.println(String.format("%-10s %-30s $%-18.2f", installment_id, description,payment_amount));
                        } while(rs.next());
                    }

                }   
                catch(SQLException e)
                {
                    System.out.println("Error Querying for payments transactions " + e.getMessage());
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error Querying for payment transactions " + e.getMessage());
            }

        }


        else
        {
            String bankInformationQuery =
            "WITH busTrans AS (" +
            "    SELECT * FROM bus_transaction WHERE customer_id = ?" +
            ") " +
            "SELECT * FROM service s JOIN busTrans b ON s.product_id = b.product_id";
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");
            try(PreparedStatement businessStatement = conn.prepareStatement(bankInformationQuery))
            {
                businessStatement.setInt(1, userId);
                try(ResultSet rs = businessStatement.executeQuery())
                {
                    System.out.println("\n\n------------------------------Service Purchases Made------------------------------\n");
                    if(!rs.next())
                    {
                        System.out.println("No Service Purchases have been made by this account");
                    }
                    else
                    {
                        System.out.println(String.format("%-30s %-18s %-10s %-15s %-18s","Description", "Vendor", "Price", "Date of Order", "Duration (minutes)"));
                        do
                        {
                            String description = rs.getString("DESCRIPTION");
                            String vendor = rs.getString("VENDOR");
                            double price = rs.getDouble("PRICE");
                            String dateofOrder = "";
                            try {
                                java.sql.Date deliverySqlDate = rs.getDate("DATE_OF_ORDER");
                                if (deliverySqlDate != null) 
                                {
                                   dateofOrder = dateFormat.format(deliverySqlDate);
                                }
                            } 
                            catch (Exception e)
                            {
                                dateofOrder = rs.getString("DATE_OF_DELIVERY"); 
                            }
                            double duration = rs.getDouble("Duration");

                            duration = duration * 60;

                            System.out.println(String.format("%-30s %-18s $%-9.2f %-15s %-17.2f", description, vendor, price, dateofOrder, duration));
                        } while(rs.next());
                    }

                }   
                catch(SQLException e)
                {
                    System.out.println("Error Querying for service transactions");
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error Querying for service transactions");
            }
        }
    }


    public static void seeTotalExpenses(int userId, Connection conn, boolean isInd)
    {
        double total = 0;
        if(isInd)
        {
            String financedItemsQuery =
            "WITH financedTransactions AS (" +
            "    SELECT * FROM financed_transaction WHERE customer_id = ?" +
            ") " +
            "SELECT price FROM item I JOIN financedTransactions F ON I.product_id = F.product_id";
           
            try(PreparedStatement financedStatement = conn.prepareStatement(financedItemsQuery))
            {
                financedStatement.setInt(1, userId);
                try(ResultSet rs = financedStatement.executeQuery())
                {
                    
                    if(!rs.next())
                    {
                    }
                    else
                    {
                        do
                        {
                            double price = rs.getDouble("PRICE");
                            total += price;
                        } while(rs.next());
                    }

                }   
                catch(SQLException e)
                {
                    System.out.println("Error Querying for financed transactions");
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error Querying for financed transactions");
            }


            String unfinancedItemsQuery =
            "WITH unfinancedTransactions AS (" +
            "    SELECT * FROM unfinanced_transaction WHERE customer_id = ?" +
            ") " +
            "SELECT price FROM item I JOIN unfinancedTransactions U ON I.product_id = U.product_id";

            try(PreparedStatement unfinancedStatement = conn.prepareStatement(unfinancedItemsQuery))
            {
                unfinancedStatement.setInt(1, userId);
                try(ResultSet rs = unfinancedStatement.executeQuery())
                {
                    if(!rs.next())
                    {
                    }
                    else
                    {
                        do
                        {
                            double price = rs.getDouble("PRICE");
                            total+=price;
                        } while(rs.next());
                    }

                }   
                catch(SQLException e)
                {
                    System.out.println("Error Querying for unfinanced transactions");
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error Querying for unfinanced transactions");
            }
            
            System.out.println("\n---------------------- Total Account Spending ----------------------");
            System.out.printf("Total spent on LUSHOP: $%.2f\n\n", total);
            System.out.println("-------------------------------------------------------------------");
        }
        else
        {
            String bankInformationQuery =
            "WITH busTrans AS (" +
            "    SELECT * FROM bus_transaction WHERE customer_id = ?" +
            ") " +
            "SELECT price FROM service s JOIN busTrans b ON s.product_id = b.product_id";

            try(PreparedStatement bankStatement = conn.prepareStatement(bankInformationQuery))
            {
                bankStatement.setInt(1, userId);
                try(ResultSet rs = bankStatement.executeQuery())
                {
                    if(!rs.next())
                    {
                    }
                    else
                    {
                        do
                        {
                            double price = rs.getDouble("PRICE");
                            total+=price;
                        } while(rs.next());
                    }

                }   
                catch(SQLException e)
                {
                    System.out.println("Error Querying for business transactions   " + e.getMessage());
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error Querying for business transactions   " + e.getMessage());
            }
            
            System.out.println("\n---------------------- Total Account Spending ----------------------");
            System.out.printf("Total spent on LUSHOP: $%.2f\n\n", total);
            System.out.println("-------------------------------------------------------------------");
        }
        
    }
}  

