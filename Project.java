import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

// INTERFACE
interface Payment {
    boolean pay(int amount);
}

// ABSTRACT CLASS
abstract class Movie {
    String name;
    Movie(String name){ this.name = name; }
    abstract void show();
}

// INHERITANCE
class RegularMovie extends Movie {
    RegularMovie(String name){ super(name); }
    void show(){ System.out.println(name); }
}

// COMPOSITION
class Booking {
    Movie movie;
    String date,time;
    ArrayList<String> seats;

    Booking(Movie m,String d,String t,ArrayList<String> s){
        movie=m;
        date=d;
        time=t;
        seats=new ArrayList<>(s);
    }

    int total(){
        return seats.size()*400;
    }
}

// PAYMENT
class CashPayment implements Payment {
    public boolean pay(int amount){
        try{
            String input = JOptionPane.showInputDialog("Total = "+amount+"\nEnter payment:");
            int paid = Integer.parseInt(input);

            if(paid < amount){
                JOptionPane.showMessageDialog(null,"Not enough money!");
                return false;
            }

            int change = paid - amount;

            if(change>0){
                JOptionPane.showMessageDialog(null,"Change: "+change);
            }

            JOptionPane.showMessageDialog(null,"Thank You!");
            return true;

        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Error!");
            return false;
        }
    }
}

// MAIN CLASS
public class Project
{

    static Scanner sc = new Scanner(System.in);

    static String[] times = {"10 AM","1:30 PM","5 PM","8:30 PM"};

    static Movie[] movies = {
            new RegularMovie("Prince: Once Upon a Time in Dhaka"),
            new RegularMovie("Domm"),
            new RegularMovie("Rakkhosh"),
            new RegularMovie("Bonolota Express"),
            new RegularMovie("Pressure Cooker")
    };

    public static void main(String[] args){

        System.out.println("Welcome to Star Cineplex");
        sc.nextLine();

        ArrayList<String> dates = generateDates();

        System.out.println("\nSelect Date:");
        for(int i=0;i<dates.size();i++){
            System.out.println((i+1)+". "+dates.get(i));
        }
        int d = sc.nextInt()-1;

        // MOVIE
        System.out.println("\nSelect Movie:");
        for(int i=0;i<movies.length;i++){
            System.out.println((i+1)+". "+movies[i].name);
        }
        int m = sc.nextInt()-1;

        // TIME FILTER
        LocalDate today = LocalDate.now();
        LocalDate selectedDate = today.plusDays(d);
        LocalTime now = LocalTime.now();

        System.out.println("\nSelect Showtime:");

        ArrayList<Integer> validTimes = new ArrayList<>();

        for(int i=0;i<times.length;i++){

            LocalTime showTime;

            if(i==0) showTime = LocalTime.of(10,0);
            else if(i==1) showTime = LocalTime.of(13,30);
            else if(i==2) showTime = LocalTime.of(17,0);
            else showTime = LocalTime.of(20,30);

            if(selectedDate.equals(today)){
                if(showTime.isAfter(now)){
                    validTimes.add(i);
                    System.out.println(validTimes.size()+". "+times[i]);
                }
            } else {
                validTimes.add(i);
                System.out.println(validTimes.size()+". "+times[i]);
            }
        }

        int choice = sc.nextInt()-1;
        int t = validTimes.get(choice);

        new SeatUI(movies[m], dates.get(d), times[t]);
    }

    // ✅ SMART DATE GENERATION
    static ArrayList<String> generateDates(){

        ArrayList<String> list = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        LocalTime[] showTimes = {
                LocalTime.of(10,0),
                LocalTime.of(13,30),
                LocalTime.of(17,0),
                LocalTime.of(20,30)
        };

        boolean hasFutureShow = false;

        for(LocalTime t : showTimes){
            if(t.isAfter(now)){
                hasFutureShow = true;
                break;
            }
        }

        int startDay = hasFutureShow ? 0 : 1;

        for(int i=0;i<3;i++){
            list.add(today.plusDays(startDay + i).format(formatter));
        }

        return list;
    }

    // GUI CLASS
    static class SeatUI extends JFrame {

        HashSet<String> booked = new HashSet<>();
        ArrayList<String> selected = new ArrayList<>();

        Movie movie;
        String date,time;

        int rows=8, perSide=5;

        SeatUI(Movie m,String d,String t){

            movie=m;
            date=d;
            time=t;

            setTitle("Star Cineplex");
            setSize(800,600);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(EXIT_ON_CLOSE);

            JLabel screen = new JLabel("THEATRE SCREEN",SwingConstants.CENTER);
            screen.setOpaque(true);
            screen.setBackground(Color.LIGHT_GRAY);
            add(screen,BorderLayout.NORTH);

            loadSeats();

            if(booked.isEmpty()){
                generateBooked();
            }

            JPanel main = new JPanel(new GridLayout(rows,1,5,5));

            for(int i=0;i<rows;i++){
                JPanel row = new JPanel(new GridLayout(1,perSide*2+1,5,5));
                char r = (char)('A'+i);

                for(int j=1;j<=perSide;j++)
                    row.add(createSeat(r+""+j));

                row.add(new JLabel(""));

                for(int j=perSide+1;j<=perSide*2;j++)
                    row.add(createSeat(r+""+j));

                main.add(row);
            }

            add(new JScrollPane(main),BorderLayout.CENTER);

            JButton book = new JButton("Confirm Booking");
            book.addActionListener(e->confirm());

            add(book,BorderLayout.SOUTH);

            setVisible(true);
        }

        JButton createSeat(String name){
            JButton b = new JButton(name);

            if(booked.contains(name)){
                b.setBackground(Color.RED);
                b.setEnabled(false);
            } else {
                b.setBackground(Color.WHITE);
            }

            b.addActionListener(e->{

                if(b.getBackground()==Color.GREEN){
                    b.setBackground(Color.WHITE);
                    selected.remove(name);
                } else {

                    if(selected.size()==10){
                        JOptionPane.showMessageDialog(this,"Max 10 seats!");
                        return;
                    }

                    b.setBackground(Color.GREEN);
                    selected.add(name);
                }
            });

            return b;
        }

        String getFileName(){
            return movie.name.replaceAll(" ","")+""+date.replaceAll(" ","")+"_"+time.replaceAll(" ","")+".txt";
        }

        void loadSeats(){
            try{
                File file = new File(getFileName());
                if(!file.exists()) return;

                Scanner fileSc = new Scanner(file);
                while(fileSc.hasNextLine()){
                    booked.add(fileSc.nextLine());
                }
                fileSc.close();

            }catch(Exception e){
                System.out.println("Load error");
            }
        }

        void saveSeats(){
            try{
                FileWriter fw = new FileWriter(getFileName());
                for(String s: booked){
                    fw.write(s+"\n");
                }
                fw.close();
            }catch(Exception e){
                System.out.println("Save error");
            }
        }

        void generateBooked(){
            Random r = new Random();
            int total = rows*perSide*2;
            int count = (int)(total*0.4);

            while(booked.size()<count){
                char row = (char)('A'+r.nextInt(rows));
                int num = r.nextInt(perSide*2)+1;
                booked.add(row+""+num);
            }
        }

        void confirm(){

            if(selected.isEmpty()){
                JOptionPane.showMessageDialog(this,"Select seats!");
                return;
            }

            Booking b = new Booking(movie,date,time,selected);
            int total = b.total();

            JOptionPane.showMessageDialog(this,
                    "Movie: "+movie.name+
                            "\nDate: "+date+
                            "\nTime: "+time+
                            "\nSeats: "+selected+
                            "\nTotal: "+total);

            Payment p = new CashPayment();
            boolean success = p.pay(total);

            if(success){
                booked.addAll(selected);
                saveSeats();

                selected.clear();

                dispose();
                new SeatUI(movie,date,time);
            }
        }
    }
}