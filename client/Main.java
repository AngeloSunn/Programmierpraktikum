import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JTextArea;

public class Main extends Thread {

    private String serverName;
    private String userName;
    private String pwd;
    private Socket socket;
    private JTextArea chat;
    private JTextArea userList;
    private ObjectOutputStream out;
    private LinkedList<BufferedImage> images;

    public Main(String serverName, LinkedList<BufferedImage> imgs) {
        this.serverName = serverName;
        this.images = imgs;
    }

    public void run() {
        
        try {
            this.socket = new Socket(serverName, 1234);

            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());

            Object msg;

            msg = input.readObject();
            
            String requestUsername = "";

            if (msg instanceof String) {
                requestUsername = msg.toString();
            }
                
            if(requestUsername.equals("Nutzernamen eingeben:")) {
                out.writeObject(this.userName);
                out.flush();
            }

            msg = input.readObject();
            String requestPwd = "";
            
            if (msg instanceof String) {
                requestPwd = msg.toString();
            }

            if(requestPwd.equals("Log dich mit deinem Passwort ein:") ||
                requestPwd.equals("Registriere dich mit deinem Passwort:")) {
                out.writeObject(this.pwd);
                out.flush();
                System.out.println(requestPwd);
            }
           
           while(!socket.isClosed()) {
            
            
            msg = input.readObject();

            if (msg instanceof Message) {
                System.out.print(((Message)msg).type);
            } else {
                System.out.print("Not a Message");
            }
            
            if (msg instanceof String) {
                String response = msg.toString();
                chat.setText(chat.getText() + response + "\n");
                System.out.println(response);

                if (response.equals("exit")) {
                out.writeObject(response);
                socket.close();
                }
            }
            if (msg instanceof Message && ((Message)msg).type.equals("String")) {
                String response = msg.toString();
                userList.setText(response);
            }
            boolean isImage = false;
            Pattern format = Pattern.compile(".*.png");
            boolean isPdf = false;
            Pattern formatPdf = Pattern.compile(".*.pdf");
            
            if (msg instanceof Message) {
                Matcher matcher = format.matcher(((Message)msg).type);
                isImage = matcher.matches();
                matcher = formatPdf.matcher(((Message)msg).type);
                isPdf = matcher.matches();
            }
            
            System.out.println(isImage);
            if (msg instanceof Message && isImage) {
                Message img = (Message)msg;
                

                System.out.println("received a BufferedImage");
                try {
                    File out = new File("../hase.png");
                    
                    BufferedImage newImg = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode((String)img.msg)));
                    ImageIO.write(newImg, "png", out);
                    images.add(newImg);
                } catch (IOException e) {
                    System.out.println("IOExeption occured sending file");
                }
            }

            if (msg instanceof Message && isPdf) {
                Message img = (Message)msg;
                

                System.out.println("received a BufferedImage");
                try {
                    File out = new File("../a.out.pdf");
                    byte[] pdf = Base64.getDecoder().decode((String)img.msg);
                    FileOutputStream fis = new FileOutputStream(out);
                    fis.write(pdf);  
                    fis.close();
                } catch (IOException e) {
                    System.out.println("IOExeption occured sending file");
                }
            } else {
                System.out.println("Not a Pdf");
            }
        }
            
        } catch (IOException ie) {
            System.out.println("IOException occured in client main: " + ie + ie.getStackTrace());
        } catch (ClassNotFoundException ce) {
            System.out.println("ClassNotFoundException occured in client main: " + ce.getStackTrace());
        }catch (Exception e) {
            System.out.println("Exception occured in client main: " + e.getStackTrace() + e);
        } 
    }

    public void send(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("IOException occurd in Main" + e);
        }
    }

    public void sendPic(String msg) {
        File f = new File(msg);
        BufferedImage bimg;
        
        if (f.isFile()) {
            System.out.println("Send: " + msg);
            try {
                bimg = ImageIO.read(f);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bimg, "png", baos);
                baos.close();
                Message pic = new Message(msg, Base64.getEncoder().encodeToString(baos.toByteArray()));
                out.writeObject(pic);
                out.flush();
            
            } catch (IOException e) {
                System.out.println("IOException occurd in Main" + e);
            }
        }
        
    }

    public void sendPdf(String msg) {
        File f = new File(msg);
        BufferedImage bimg;
        
        if (f.isFile()) {
            System.out.println("Send: " + msg);
            try {
                byte[] pdf = Files.readAllBytes(Paths.get(msg));
                Message pic = new Message(msg, Base64.getEncoder().encodeToString(pdf));
                out.writeObject(pic);
                out.flush();
            
            } catch (IOException e) {
                System.out.println("IOException occurd in Main" + e);
            }
        }
        
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setChat(JTextArea chat) {
        this.chat = chat;
    }

    public void setUserList(JTextArea userList) {
        this.userList = userList;
    }
}