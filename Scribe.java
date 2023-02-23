import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.nio.file.*;
import java.io.*;

public class Scribe {
	// current file (used for save)
	private static File currentFile = null;
	private static int lineNum = 1;
	private static int colNum = 1;
	private static String statusBarText = "Untitled" +" | " + "Line " + lineNum + ", Col " + colNum;

	private static void setCurrentFile(File file) {
		currentFile = file;
		String path = file != null ? file.getAbsolutePath() : "";
		statusBarText = path + " | " + "Line " + lineNum + ", Col " + colNum;
	}

	// file chooser
	private static JFileChooser fileChooser = new JFileChooser();

	// New file method
	private static void newFile() {
		// set filechooser to only choose directories
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		// show the file chooser dialog box
		int result = fileChooser.showSaveDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			// user has chosen directory, so we can make a new file there
			File selectedFile = fileChooser.getSelectedFile();
			String fileName = JOptionPane.showInputDialog("Enter file name:");

			if (fileName != null && !fileName.isEmpty()) {
				File newFile = new File(selectedFile, fileName + ".txt");
				try {
					if (newFile.createNewFile()) {
						System.out.println("New file created: " + newFile.getName());
						setCurrentFile(newFile);
					} else {
						System.out.println("File already exists.");
					}
				}
				catch (IOException e) {
					System.out.println("Error");
					e.printStackTrace();
				}
			}
		}
	}

	// Open file method
	private static String openFile() {
		// to be returned:
		StringBuilder content = new StringBuilder();

		// set filechooser to choose only files
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// show filechooser dialog box
		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			// user has chosen file, so we can open it
			File selectedFile = fileChooser.getSelectedFile();
			setCurrentFile(selectedFile);
			try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
				String line;
				while ((line = br.readLine()) != null) {
					content.append(line).append("\n");
				}
			}
			catch (IOException e) {
				System.out.println("Error");
				e.printStackTrace();
			}
		}
		
		// return built string
		return content.toString();
	}

	// Save file method
	private static void saveFile(String content) {
		// check to see if there isn't yet a defined file
		if (currentFile == null) {
			newFile();
		}
		
		// there is now a filepath to save to
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentFile))) {
			bw.write(content, 0, content.length());
		}
		catch (IOException e) {
			System.out.println("Error");
			e.printStackTrace();
		}
	}


	// Save As file method
	private static void saveAsFile(String content) {
		newFile();
		saveFile(content);
	}


	// GUI configuration
	private static void createAndShowGUI() {
		// create/setup new window
		JFrame frame = new JFrame("Scribe");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		// create textArea to put in content pane
		JTextArea textArea = new JTextArea();

		// add label at bottom of screen to indicate file name and ln/col
		JLabel statusLabel = new JLabel();
		statusLabel.setPreferredSize(new Dimension(200, 20));
		statusLabel.setText(statusBarText);
		
		// add caret listener for ln/col indicator
		textArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				// caret position
				int pos = e.getDot();

				// update ln/col of caret
				try {
					lineNum = textArea.getLineOfOffset(pos) + 1;
					colNum = pos - textArea.getLineStartOffset(lineNum - 1) + 1;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				// update statusBar
				String filePath = currentFile != null ? currentFile.getAbsolutePath() : "Untitled";
				statusBarText = filePath + " | " + "Line " + lineNum + ", Col " + colNum;
				statusLabel.setText(statusBarText);
			}
		});

		// implement scrolling
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		scrollPane.setPreferredSize(new Dimension(200, 180));

		// !! MENUS !!
		// create menu bar
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		menuBar.setBackground(new Color(240,240,255));
		menuBar.setPreferredSize(new Dimension(200,20));
		
		// create File menu
		JMenu fileMenu = new JMenu("File");

		// new file
		JMenuItem newMenuItem = new JMenuItem("New");
		newMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newFile();
			}
		});
		
		// open file
		JMenuItem openMenuItem = new JMenuItem("Open");
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText(openFile());
			}
		});
		
		// save file
		JMenuItem saveMenuItem = new JMenuItem("Save");	
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile(textArea.getText());
			}
		});
		
		// save as file
		JMenuItem saveAsMenuItem = new JMenuItem("Save As");
		saveAsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsFile(textArea.getText());
			}
		});

		fileMenu.add(newMenuItem);
		fileMenu.add(openMenuItem);
		fileMenu.add(saveMenuItem);
		fileMenu.add(saveAsMenuItem);

		// create Edit menu
		JMenu editMenu = new JMenu("Edit");
		
		// cut text
		JMenuItem cutMenuItem = new JMenuItem("Cut");	
		cutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.cut();
			}
		});
		
		// copy text
		JMenuItem copyMenuItem = new JMenuItem("Copy");
		copyMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.copy();
			}
		});
		
		// paste text
		JMenuItem pasteMenuItem = new JMenuItem("Paste");
		pasteMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.paste();
			}
		});
		
		editMenu.add(cutMenuItem);
		editMenu.add(copyMenuItem);
		editMenu.add(pasteMenuItem);
		
		// create settings menu
		JMenu settingsMenu = new JMenu("Settings");

		// text wrap setting
		JMenuItem textWrapMenuItem = new JCheckBoxMenuItem("Text Wrapping");
		textWrapMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setLineWrap(!textArea.getLineWrap());
			}
		});

		settingsMenu.add(textWrapMenuItem);

		// add menus to bar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(settingsMenu);

		// set the menu bar and add the label to the content pane
		frame.setJMenuBar(menuBar);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		frame.add(statusLabel, "South");


		// display the window
		frame.pack();
		frame.setVisible(true);

	}
	


	// main method
	public static void main(String[] args) {
		// schedule a job for the event-dispatching thread
		// creating and showing this app's GUI
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}

