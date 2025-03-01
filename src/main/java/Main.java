import Models.Appointment;
import Models.Doctor;
import Models.Client;
import Models.Insurance;
import Services.DbService;


import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) {

        DbFunctions db = new DbFunctions();
        Connection connection = db.connect_to_db("clinicdb", "root", "root");

//        ClientService clientService = new ClientService();
//        DoctorService doctorService = new DoctorService();
//        AppointmentService appointmentService = new AppointmentService(clientService, doctorService);

        DbService dbService = DbService.getInstance();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nChoose an operation:");
            System.out.println("1. Add Client");
            System.out.println("2. Add Appointment ");
            System.out.println("3. View Appointments for Client");
            System.out.println("4. View Client Info");
            System.out.println("5. View All Clients");
            System.out.println("6. Edit Client");
            System.out.println("7. Remove a Client");
            System.out.println("8. Add Doctor");
            System.out.println("9. View Appointments for Doctor");
            System.out.println("10. View All Doctors");
            System.out.println("11. Edit Doctor");
            System.out.println("12. Remove a Doctor");
            System.out.println("13. Exit\n");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                //add client
                case 1:
                    int ok = 0;
                    System.out.print("Enter client's name: ");
                    String name = scanner.nextLine();

                    System.out.print("Age: ");
                    int age;
                    while(true){
                        if(scanner.hasNextInt()){
                            age = scanner.nextInt();
                            scanner.nextLine();
                            break;
                        }
                        else{
                            System.out.println("Invalid input. Please provide a valid age.");
                            scanner.nextLine();
                        }

                    }

                    System.out.print("Enter client's category( child/student/adult/senior ): ");
                    String category = scanner.nextLine();

                    System.out.print("Phone number: ");
                    String phoneNumber = scanner.nextLine();

                    System.out.println("Do you have an insurance? (y/n)");
                    String a = scanner.nextLine();


                    if(a.equals("y")){

                        System.out.print("Insurance type: ");
                        String insuranceType = scanner.nextLine();

                        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        System.out.print("Expire Date: ");
                        String expDate = scanner.nextLine();

                        Insurance insurance = new Insurance(insuranceType, expDate);
                        Client client = new Client(name, age, category, phoneNumber, insurance);
                        dbService.addClientToDatabase(connection, client);
                        //appointmentService.addClient(client);
                    }
                    else{
                        Client client = new Client(name, age, category, phoneNumber, null);
                        dbService.addClientToDatabase(connection, client);
                        //appointmentService.addClient(client);
                    }
                    System.out.println("Client added.");
                    break;



                //add appointment
                case 2:
                    System.out.println("Enter client's name:");
                    String name1 = scanner.nextLine();
                    scanner.nextLine();


                    System.out.println("Enter doctor's name:");
                    String name2 = scanner.nextLine();

                    System.out.println("Enter date (YYYY-MM-DD HH:MM): ");
                    String appStr = scanner.nextLine();
                    LocalDateTime appointmentDate = LocalDateTime.parse(appStr, formatter);

                    dbService.addAppointment(connection, name1, name2, appointmentDate);

                    break;
                case 3:
                    // view appointments for client
                    System.out.print("Enter client name: ");
                    String clientName = scanner.nextLine();
                    List<Appointment> clientAppointments = dbService.getAppointmentsForClientByName(connection, clientName);
                    if(clientAppointments.isEmpty())
                        System.out.println("No appointments for client " + clientName);
                    else{

                        for (Appointment appointment : clientAppointments) {
                            System.out.println(appointment);
                        }
                    }

                    break;

                //client info
                case 4:
                    System.out.println("Enter client's name: ");
                    String cName = scanner.nextLine();
                    dbService.viewInfo(connection, cName);
                    break;

                case 5:
                    //view all clients
                    List<Client> clients = dbService.getAllClients(connection);
                    if(clients.isEmpty()){
                        System.out.println("No clients found.");
                    }
                    else{
                        for(Client c : clients)
                            System.out.println(c);
                        System.out.println();
                    }
                    break;

                //edit client
                case 6:
                    System.out.println("Enter client's name:");
                    String editName = scanner.nextLine();
                    dbService.editClient(connection, editName);
                    break;

                //remove client
                case 7:
                    System.out.println("Enter client's name:");
                    String removeClName = scanner.nextLine();
                    //appointmentService.removeClient(removeClName);
                    dbService.removeClient(connection, removeClName);
                    break;

                //add doctor
                case 8:
                    System.out.println("Enter doctor's name: ");
                    String docName = scanner.nextLine();

                    System.out.println("Phone: ");
                    String phone = scanner.nextLine();

                    System.out.println("Specialization: ");
                    String spec = scanner.nextLine();

                    System.out.println("Employment date(YYYY-MM-dd HH:mm)");
                    String dateStr = scanner.nextLine();
                    LocalDateTime empDate = LocalDateTime.parse(dateStr, formatter);
                    Doctor doctor = new Doctor(docName, phone, spec, empDate);

                    dbService.addDoctors(connection, doctor);
                    //appointmentService.addDoctor(doctor);

                    System.out.println("Doctor added.");

                    break;
                //appointments for doctor
                case 9:
                    System.out.print("Enter doctor name: ");
                    String doctorName = scanner.nextLine();
                    List<Appointment> doctorAppointments = dbService.getAppointmentsForDoctorByName(connection, doctorName);
                    if(doctorAppointments.isEmpty())
                        System.out.println("No appointments for doctor " + doctorName);
                    else{

                        for (Appointment appointment : doctorAppointments) {
                            System.out.println(appointment);
                        }
                    }

                    break;
                //all doctors
                case 10:
                    List<Doctor> doctors = dbService.getAllDoctors(connection);
                    if(doctors.isEmpty())
                        System.out.println("No doctors found.");
                    else {
                        for (Doctor doc : doctors)
                            System.out.println(doc);
                    }
                    break;
            //edit doc
                case 11:
                    System.out.println("Enter doctor's name: ");
                    String editDocName = scanner.nextLine();
                    dbService.editDoctor(connection, editDocName);
                    break;

                //remove doc
                case 12:
                    System.out.println("Enter doctor's name: ");
                    String removeDocName = scanner.nextLine();
                    //appointmentService.removeDoctor(removeDocName);
                    dbService.removeDoc(connection, removeDocName);
                    break;


                case 13:
                    // Exit
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please choose a valid operation.");
                    break;
            }
        }
    }
}
