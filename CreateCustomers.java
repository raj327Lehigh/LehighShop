import java.sql.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;

public class CreateCustomers
{
    private static final Scanner scan = new Scanner(System.in);

    public static String[] getIndividualInformation()
    {
        System.out.print("Please Enter a customer name: ");
        String name = scan.nextLine().trim();

        System.out.print("Please Enter a shipping address: ");
        String address = scan.nextLine().trim();

        String dob;
        while (true) 
        {
            System.out.print("Please enter date of birth (YYYY-MM-DD): ");
            dob = scan.nextLine().trim();
            if (dob.matches("\\d{4}-\\d{2}-\\d{2}")) 
            {
                break;
            }
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        }

        String dateOfJoin;
        while (true) 
        {
            System.out.print("Please enter date of join (YYYY-MM-DD): ");
            dateOfJoin = scan.nextLine().trim();
            if (dateOfJoin.matches("\\d{4}-\\d{2}-\\d{2}"))
            {
                break;
            } 
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        }

        return new String[] { name, address, dob, dateOfJoin };
    }

    public static void createNewIndividualUser(Connection conn)
    {
        String userInput[] = getIndividualInformation();
        int customer_id = -1;

        try
        {
            conn.setAutoCommit(false);


            String customerInsert = "INSERT INTO Customer(name, shipping_address, date_of_join) VALUES (?,?,?)";

            try(PreparedStatement paymentInsert = conn.prepareStatement(customerInsert))
            {
                paymentInsert.setString(1, userInput[0]);
                paymentInsert.setString(2, userInput[1]);
                java.sql.Date joinDate = java.sql.Date.valueOf(userInput[3]);
                paymentInsert.setDate(3, joinDate);
                paymentInsert.executeUpdate();
            }
            

            customer_id = -1; 

            String sql2 = "SELECT MAX(customer_id) from CUSTOMER";
            try(PreparedStatement payIdQuery = conn.prepareStatement(sql2))
            {
                try(ResultSet rs = payIdQuery.executeQuery())
                {
                    if(rs.next())
                    {
                        customer_id = rs.getInt(1);
                    }
                }
            }

            String indInsert = "INSERT INTO Individual VALUES (?, ?)";

            try(PreparedStatement userInsert = conn.prepareStatement(indInsert))
            {
                userInsert.setInt(1, customer_id);
                java.sql.Date dob = java.sql.Date.valueOf(userInput[2]);
                userInsert.setDate(2, dob);
                userInsert.executeUpdate(); 
            }
            conn.commit();

            System.out.println("\n-----All Accounts Must Have a Bank Assocaited or Credit Card------");
            
            
        }
        catch (SQLException e)
        {
            try 
            {
                conn.rollback(); 
            } 
            catch (SQLException ex) 
            {
                //Nothing to do but fail here but this shouldlnt happen 
            }
            System.out.println("Error adding Business account: " + e.getMessage());
            
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


        IndividualCustomer.addCustomerPaymentMethods(customer_id, conn, true);
        System.out.println("\nIndividual account and bank account added successfully!\n");
    }



















































    public static String[] getBusinessInformation()
    {
       
        System.out.print("Please Enter your business name: ");
        String name = scan.nextLine().trim();

        String foundationDate;
        while (true) 
        {
            
            System.out.print("Please enter date of foundation (YYYY-MM-DD): ");
            foundationDate = scan.nextLine().trim();
            if (foundationDate.matches("\\d{4}-\\d{2}-\\d{2}")) 
            {
                break;
            }
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        }

        System.out.print("Please Enter a shipping address: ");
        String address = scan.nextLine().trim();

        String[] validTypes = {"LLC", "CORPORATION", "S-CORP", "PARTNERSHIP", "SOLE PROPRIETORSHIP"}; //Could add more to this later
        String corporationType; //holder for the corporation type
        while (true) 
        {
            System.out.print("Please enter corporation type (LLC, CORPORATION, S-CORP, PARTNERSHIP, SOLE PROPRIETORSHIP): ");
            corporationType = scan.nextLine().trim().toUpperCase();
            boolean valid = false;
            for (String type : validTypes) {
                if (corporationType.equals(type)) 
                {
                    valid = true;
                    break;
                }
            }
            if (valid) 
            {
                break;
            }
            System.out.println("Invalid corporation type. Must be one of: LLC, CORPORATION, S-CORP, PARTNERSHIP, SOLE PROPRIETORSHIP.");
        }

        int numLocationsNum;
        StringBuilder prompt = new StringBuilder("Please enter number of locations of your business:");
        numLocationsNum = Main.getIntInRange(prompt,  0, 999);
        String numLocations = Integer.toString(numLocationsNum);


        String dateOfJoin;
        while (true) 
        {
            System.out.print("Please enter date of join (YYYY-MM-DD): ");
            dateOfJoin = scan.nextLine().trim();
            if (dateOfJoin.matches("\\d{4}-\\d{2}-\\d{2}"))
            {
                break;
            } 
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        }

        return new String[] {name, foundationDate, address, corporationType, numLocations, dateOfJoin };
    }



    public static void createNewBusinessUser(Connection conn)
    {
        String userInput[] = getBusinessInformation();

        try
        {
            conn.setAutoCommit(false);

            String customerInsert = "INSERT INTO Customer(name, shipping_address, date_of_join) VALUES (?,?,?)";

            try(PreparedStatement paymentInsert = conn.prepareStatement(customerInsert))
            {
                paymentInsert.setString(1, userInput[0]);
                paymentInsert.setString(2, userInput[2]);
                java.sql.Date joinDate = java.sql.Date.valueOf(userInput[5]);
                paymentInsert.setDate(3, joinDate);
                paymentInsert.executeUpdate();
            }
            

            int customer_id = -1; 

            String sql2 = "SELECT MAX(customer_id) from CUSTOMER";
            try(PreparedStatement payIdQuery = conn.prepareStatement(sql2))
            {
                try(ResultSet rs = payIdQuery.executeQuery())
                {
                    if(rs.next())
                    {
                        customer_id = rs.getInt(1);
                    }
                }
            }

            String businessInsert = "INSERT INTO Business VALUES (?, ?, ?, ?)";

            try(PreparedStatement bankInsert = conn.prepareStatement(businessInsert))
            {
                bankInsert.setInt(1, customer_id);
                java.sql.Date foundDate = java.sql.Date.valueOf(userInput[1]);
                bankInsert.setDate(2, foundDate);
                bankInsert.setString(3, userInput[3]);
                bankInsert.setString(4, userInput[4]);
                bankInsert.executeUpdate(); 
            }


            System.out.println("\n-----All Accounts Must Have a Bank Assocaited------");
            CustomerHelpers.addCustomerBankAccount(customer_id, conn, true);
            System.out.println("\nBusiness account and bank account added successfully!\n");
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
                //Nothing to do but fail here but this shouldlnt happen 
            }
            System.out.println("Error adding Business account: " + e.getMessage());
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
} 

