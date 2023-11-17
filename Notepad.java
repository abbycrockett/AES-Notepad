import java.awt.*;
import java.io.*;
import java.net.URI;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class Notepad extends JFrame implements ActionListener {
	
    JTextArea area;
    JScrollPane pane;
    String filename;
    JMenuBar menuBar;
    JMenu file, edit, help;
    JMenuItem open, save, saveas, newfile, exit, cut, copy, paste, source, about;
    JCheckBoxMenuItem encrypt;
    String encryptedTag = "(this is an encrypted file)";
    String salt = "YouaretheSaltoftheEarth";

    
//BUILD UI FOR NOTEPAD:------------------------------------------------------
    Notepad() {
    	this.setTitle("AES NOTEPAD");
    	
    	//PANE:
        area = new JTextArea();
        pane = new JScrollPane(area);
        pane.setBorder(BorderFactory.createEmptyBorder());
        add(pane, BorderLayout.CENTER);
             
        //MENU INFO:
        Color menuBarColor = new Color(209, 243, 255);
        menuBar = new JMenuBar();
        menuBar.setBackground(menuBarColor);
        file = new JMenu("File");
        edit = new JMenu("Edit");
        help = new JMenu("Help");
        open = new JMenuItem("Open");
        save = new JMenuItem("Save");
        saveas = new JMenuItem("Save As");
        newfile = new JMenuItem("New");
        exit = new JMenuItem("Exit");
        cut = new JMenuItem("Cut");
        copy = new JMenuItem("Copy");
        paste = new JMenuItem("Paste");
        source = new JMenuItem("AES Docs");
        about = new JMenuItem("About Notepad");
        encrypt = new JCheckBoxMenuItem("Check to Encrypt");
        encrypt.setBackground(menuBarColor);
        file.add(newfile);
        file.add(open);
        file.add(save);
        file.add(saveas);
        file.add(exit);
        edit.add(cut);
        edit.add(copy);
        edit.add(paste);
        help.add(source);
        help.add(about);
        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(help);
        menuBar.add(encrypt);
        
        //ACTION LISTENERS:
        newfile.addActionListener(this);
        open.addActionListener(this);
        save.addActionListener(this);
        saveas.addActionListener(this);
        exit.addActionListener(this);
        cut.addActionListener(this);
        copy.addActionListener(this);
        paste.addActionListener(this);
        source.addActionListener(this);
        about.addActionListener(this);
        encrypt.addActionListener(this);
        setJMenuBar(menuBar);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    } 
    
    
  //ACTIONS:----------------------------------------------------------------
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == newfile) {
            area.setText("");
            filename = null;
            
         // START OPENING THE TEXT FILE
        } else if (e.getSource() == open) {
        	
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter restrict = new FileNameExtensionFilter("Use .txt", "txt");
            chooser.addChoosableFileFilter(restrict);
            int result = chooser.showOpenDialog(this);
            
            if (result == JFileChooser.APPROVE_OPTION) {
            	
                File file = chooser.getSelectedFile();
                filename = file.getAbsolutePath();
                
                try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                	
                    StringBuilder content = new StringBuilder();
                    String contentString = "";
                    String line;
                    Boolean isEncrypted = false;

                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }

                    // Check if the content is not null and not empty (no length)
                    if (content != null && content.length() > 0) {
                        // Check if the content starts with the tag
                        if (content.toString().startsWith(encryptedTag)) {
                            isEncrypted = true;
                            int tagLength = encryptedTag.length();
                            content.delete(0, tagLength);
                            
                            // Replace line breaks so it can decode (stackoverflow)
                            contentString = content.toString().replaceAll("\\r\\n|\\r|\\n", "");

                            // Convert the modified string back to StringBuilder
                            content = new StringBuilder(contentString);
                        }

                        if (isEncrypted) {
                            // Reset the reader to the beginning of the file
                            try (BufferedReader encryptedReader = new BufferedReader(new FileReader(filename))) {
                                // Skip the first line (it has already been read)
                                encryptedReader.readLine();

                                //Ask for the password
                                String password = JOptionPane.showInputDialog(this, "Enter password:");

                                if (password == null) {
                                    return; // User canceled password entry
                                }

                                try {
                                	
                                	 String decryptedString = AES.decryptTxt(contentString, password, salt);
                                	 
                                	 //FOR TESTING--------------------------------
                                     if (decryptedString != null) {
                                         System.out.println("Decryption successful. Decrypted message: " + decryptedString);
                                         area.setText(decryptedString);
                                     } else {
                                         System.err.println("Decryption failed. Incorrect password.");
                                     }
                                     //----------------------------------------------
                                   
                                    
                                } catch (Exception ex) {
                                    System.out.println("This is the exception msg: " + ex.getMessage());
                                }
                            }
                        } else {
                            // This is a regular text file
                            area.setText(content.toString());
                        }
                    } else {
                        // Handle the case where the file is empty
                        System.out.println("The file is empty.");
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
        // END OF OPENING TXT FILE

        else if (e.getSource() == save) {
            if (filename == null) {
                JFileChooser saveas = new JFileChooser();
                saveas.setApproveButtonText("Save");
                int actionDialog = saveas.showOpenDialog(this);
                if (actionDialog != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File fileName = new File(saveas.getSelectedFile() + ".txt");
                filename = fileName.getAbsolutePath();
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
                if (encrypt.isSelected()) {
                	//Ask for the password
                    String password = JOptionPane.showInputDialog(this, "Enter password:");
                    
                    if (password == null) {
                        return; // User canceled password entry
                    }
                    
                    String encryptedString = AES.encryptTxt(area.getText(), password, salt);
                    
                    if (encryptedString != null) 
                        System.out.println("Password received. Here's the encoded message: " + encryptedString);
                    
                    //DISPLAY THE PLAINTEXT :)
                    writer.write(encryptedTag + encryptedString);
                    
                } else {
                    area.write(writer);
                }
                writer.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        } else if (e.getSource() == saveas) {
            JFileChooser saveas = new JFileChooser();
            saveas.setApproveButtonText("Save");
            int actionDialog = saveas.showOpenDialog(this);
            if (actionDialog != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File fileName = new File(saveas.getSelectedFile() + ".txt");
            filename = fileName.getAbsolutePath();
            
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
                if (encrypt.isSelected()) {
                	
                    String password = JOptionPane.showInputDialog(this, "Enter password:");
                    
                    if (password == null) {
                        return;
                    }
                    
                    String encryptedString = AES.encryptTxt(area.getText(), password, salt);
                    
                    if (encryptedString != null) 
                        System.out.println("Password received. Here's the encoded message: " + encryptedString);
                    
                
                    writer.write(encryptedTag + encryptedString);
                } else {
                    area.write(writer);
                }
                writer.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        } else if (e.getSource() == exit) {
        	//Closes Notepad Application
            System.exit(0);
        } else if (e.getSource() == cut) {
            area.cut();
        } else if (e.getSource() == copy) {
            area.copy();
        } else if (e.getSource() == paste) {
            area.paste();
        } else if (e.getSource() == source) {
        	try {
                Desktop.getDesktop().browse(new URI("https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == about) {
            JOptionPane.showMessageDialog(this, "This is notepad app encrypts and decrypts AES text files. \nEclipse '23: Created by Abby C.", "About Notepad", JOptionPane.PLAIN_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new Notepad();
    }
}


