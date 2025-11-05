import java.sql.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;

public class IndividualCustomer {

    private static final Scanner scan = new Scanner(System.in);

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
            "Go Back"
        };

        StringBuilder promptForAccount = Main.chooseFromOptions("\nPlease select one of the following options", accountOptions);
        

        int choiceOfCustomerInterface = -1;
        while(choiceOfCustomerInterface != 6)
        {
            choiceOfCustomerInterface = Main.getIntInRange(promptForAccount, 1,6);

            switch(choiceOfCustomerInterface)
            {
                case 1:
                    getCustomerPaymentMethods(customerID,conn, true);
                    break;
                case 2:
                    addCustomerPaymentMethods(customerID, conn, true);
                    break;
                case 3:
                    seePurchaseHistory(customerID, conn, true);
                    break;
                case 4:
                    seeFinancingHistory(customerID, conn);
                    break;
                case 5:
                    seeTotalExpenses(customerID, conn, true);
                    break;
                case 6:
                    break;
            }
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
                    System.out.println("\n------------------------Bank Accounts on File------------------------");
                    if(!rs.next())
                    {
                        System.out.println("No Bank Accounts on File\n");
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

    public static void addCustomerPaymentMethods(int userId, Connection conn, boolean isInd)
    {
        if(isInd)
        {
             String[] paymentOptions =
            {
                "Add Credit Card",
                "Add Bank Account",
                "Go Back"
            };


            StringBuilder prompt = Main.chooseFromOptions("Please select what you would like to add ", paymentOptions);

            int choiceOfCustomerInterface = -1;

            while(choiceOfCustomerInterface != 3)
            {
                choiceOfCustomerInterface = Main.getIntInRange(prompt, 1,3);
                if(choiceOfCustomerInterface == 1)
                {
                    addCustomerCreditCard(userId, conn, isInd);
                }
                else if(choiceOfCustomerInterface ==2)
                {
                    addCustomerBankAccount( userId, conn,isInd);
                }
            }
           
        }
    }

    public static void addCustomerCreditCard(int userId, Connection conn, boolean isInd)
    {
        try
        {
            conn.setAutoCommit(false);

            String sql1 = "INSERT INTO Payment(user_id) VALUES (?)";
            try(PreparedStatement paymentInsert = conn.prepareStatement(sql1))
            {
                paymentInsert.setInt(1,userId);
                paymentInsert.executeUpdate();
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
            }

            String sql3 = "INSERT INTO Credit_Card (pay_id, card_number, expiration_date, cv, billing_address) VALUES (?, ?, ?, ?, ?)";
            String[] cardInfo = getCreditCardInput();
            java.sql.Date expDate = java.sql.Date.valueOf(cardInfo[1]);

            try(PreparedStatement cardInsert = conn.prepareStatement(sql3))
            {
                cardInsert.setInt(1, pay_id);
                cardInsert.setString(2, cardInfo[0]);
                cardInsert.setDate(3, expDate);
                cardInsert.setString(4, cardInfo[2]);
                cardInsert.setString(5, cardInfo[3]);
                cardInsert.executeUpdate();
            }

            conn.commit();
            System.out.println("\n\nCredit card added successfully!\n\n");
        }
        catch (SQLException e)
        {
            try 
            {
                conn.rollback(); 
            } 
            catch (SQLException ex) 
            {
                //Nothing to do but fail here but this should happen 
            }
            System.out.println("Error adding credit card: " + e.getMessage());
        }
        finally
        {
            try 
            {
                conn.setAutoCommit(true); 
            } 
            catch (SQLException ex) 
            {

            }
        }
    }

    public static String[] getCreditCardInput()
    {
        String cardNumber;
        while(true)
        {
            System.out.print("Enter credit card number (Must be 13-19 digits) (If you want to just copy and paste one use 1234567890123):");
            cardNumber = scan.nextLine().trim();
            if (cardNumber.matches("\\d{13,19}"))
            {
                break;
            }
            System.out.println("Invalid card number. Must be 13-19 digits.");
        }

        String expirationDate;
        while(true)
        {
            System.out.print("Enter expiration date (YYYY-MM-DD):");
             expirationDate = scan.nextLine().trim();
            if (expirationDate.matches("\\d{4}-\\d{2}-\\d{2}")) 
            {
                break;
            }
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        }

        String cv;
        while (true) 
        {
            System.out.print("Enter CV (3 or 4 digits): ");
            cv = scan.nextLine().trim();

            if (cv.matches("\\d{3,4}")) 
            {
                break;
            }
            System.out.println("Invalid CVV. Must be 3 or 4 digits.");
        }

        String billingAddress;
        while (true) 
        {
            System.out.print("Enter billing address: ");
            billingAddress = scan.nextLine().trim();
            if (!billingAddress.isEmpty()) 
            {
                break;
            }
            System.out.println("Billing address cannot be empty.");
        }

        return new String[] { cardNumber, expirationDate, cv, billingAddress };

    }

    public static void addCustomerBankAccount(int userId, Connection conn, boolean isInd)
    {
        try
        {
            conn.setAutoCommit(false);

            String sql1 = "INSERT INTO Payment(user_id) VALUES (?)";
            try(PreparedStatement paymentInsert = conn.prepareStatement(sql1))
            {
                paymentInsert.setInt(1,userId);
                paymentInsert.executeUpdate();
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

            conn.commit();
            System.out.println("\n\nBank account added successfully!\n\n");
        }
        catch (SQLException e)
        {
            try 
            {
                conn.rollback(); 
            } 
            catch (SQLException ex) 
            {
                //Nothing to do but fail here but this should happen 
            }
            System.out.println("Error adding bank account: " + e.getMessage());
        }
        finally
        {
            try 
            {
                conn.setAutoCommit(true); 
            } 
            catch (SQLException ex) 
            {

            }
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
                    System.out.println("\n\n------------------------------Financed Purchases Made------------------------------\n");
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


        }
    }

    public static void seeFinancingHistory(int userId, Connection conn)
    {
        String sql = 
        "WITH subtable AS ( " +
        "    SELECT * " +
        "    FROM financed_transaction tran " +
        "    JOIN financing fp USING (plan_id) " +
        ") " +
        "SELECT " +
        "    product_id, " +
        "    description, " +
        "    installment_id, " +
        "    contract, " +
        "    number_installments, " +
        "    price, " +
        "    remaining_balance, " +
        "    interest_rate, " +
        "    payment_amount " +
        "FROM transaction_for " +
        "JOIN subtable USING (transaction_id) " +
        "JOIN item USING (product_id) " +
        "WHERE customer_id = ?";
        try(PreparedStatement present = conn.prepareStatement(sql))
        {
            present.setInt(1, userId);


            System.out.println("-------------------------Account Financing History-------------------------");
            try(ResultSet rs = present.executeQuery())
            {
                if(!rs.next())
                {
                    System.out.println("\nThis account has no financing history associated\n");
                }
                else
                {
                    System.out.printf(
                        "%-6s %-25s %-15s %-25s %-12s %-12s %-20s %-15s %-15s%n",
                        "PID", "Description", "Installment #", "Contract", "Num Inst.", "Price", "Remaining Balance", "Interest", "Payment"
                    );
                    do
                    {
                        int productId = rs.getInt("product_id");
                        String description = rs.getString("description");
                        int installmentId = rs.getInt("installment_id");
                        String contract = rs.getString("contract");
                        int numInstallments = rs.getInt("number_installments");
                        double price = rs.getDouble("price");
                        double remainingBalance = rs.getDouble("remaining_balance");
                        double interestRate = rs.getDouble("interest_rate");
                        double paymentAmount = rs.getDouble("payment_amount");

                        System.out.printf(
                            "%-6d %-25s %-15d %-25s %-12d $%-11.2f $%-19.2f %-15.2f $%-14.2f%n",
                            productId, description, installmentId, contract,
                            numInstallments, price, remainingBalance, interestRate, paymentAmount
                        );
                    }while(rs.next());
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error Iterating through results.");
            }

        }
        catch(SQLException e)
        {
            System.out.println("Error gathering card information.");
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
        
    }
}