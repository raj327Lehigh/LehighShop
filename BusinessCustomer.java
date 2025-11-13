import java.sql.*;
import java.util.Scanner;

public class BusinessCustomer
{
    private static final Scanner scan = new Scanner(System.in);

    public static void businessInterface(Connection conn)
    {
        
        CustomerHelpers.currentUserList(conn, false); //Lists the current users with a nice message
        int customerID = CustomerHelpers.signUserIn(conn, 2); //Signs the user in and returns the customer ID
        String name = CustomerHelpers.getName(conn, customerID); //gets the customers name for use in the appearance

        System.out.println("\n\n-------------Welcome " + name + "-------------");

        System.out.println("What would you like to do with your account?\n");

        String[] accountOptions =
        {
            "See Payment Methods",
            "Add a Payment Method",
            "See Purchases",
            "See Total Expenses",
            "Go Back"
        };

        StringBuilder promptForAccount = Main.chooseFromOptions("\nPlease select one of the following options", accountOptions);
        

        int choiceOfCustomerInterface = -1;
        while(choiceOfCustomerInterface != 5)
        {
            choiceOfCustomerInterface = Main.getIntInRange(promptForAccount, 1,5);

            switch(choiceOfCustomerInterface)
            {
                case 1:
                    CustomerHelpers.getCustomerPaymentMethods(customerID,conn, false);
                    break;
                case 2:
                    try
                    {
                        conn.setAutoCommit(false);
                        CustomerHelpers.addCustomerBankAccount(customerID, conn, true);
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
                case 3:
                    CustomerHelpers.seePurchaseHistory(customerID, conn, false);
                    break;
                case 4: 
                    CustomerHelpers.seeTotalExpenses(customerID, conn, false);
                    break;
                case 5:
                    break;
            }
        }
    }
}