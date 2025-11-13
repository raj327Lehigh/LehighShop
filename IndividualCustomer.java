import java.sql.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;

public class IndividualCustomer {

    private static final Scanner scan = new Scanner(System.in);

    public static void individualInterface(Connection conn)
    {
        
        CustomerHelpers.currentUserList(conn, true); //Lists the current users with a nice message
        int customerID = CustomerHelpers.signUserIn(conn, 1); //Signs the user in and returns the customer ID
        String name = CustomerHelpers.getName(conn, customerID); //gets the customers name for use in the appearance

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
                    CustomerHelpers.getCustomerPaymentMethods(customerID,conn, true);
                    break;
                case 2:
                    addCustomerPaymentMethods(customerID, conn, true);
                    break;
                case 3:
                    CustomerHelpers.seePurchaseHistory(customerID, conn, true);
                    break;
                case 4:
                    seeFinancingHistory(customerID, conn);
                    break;
                case 5:
                    CustomerHelpers.seeTotalExpenses(customerID, conn, true);
                    break;
                case 6:
                    break;
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
                    try
                    {
                        conn.setAutoCommit(false);
                        CustomerHelpers.addCustomerBankAccount( userId, conn, isInd);
                        conn.commit();
                    }
                    catch (SQLException e)
                    {
                        try 
                        {
                            conn.rollback(); 
                        } 
                        catch (SQLException ex) 
                        {
                            System.out.println("Error entering user bank account info : " + ex.getMessage());
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
                            System.out.println("Error entering user bank account info : " + ex.getMessage());
                        }
                    }
                    break;
                    
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

    
}