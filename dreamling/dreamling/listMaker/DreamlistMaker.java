package dreamling.listMaker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import GUI.windowTools.CustomScrollPane;
import GUI.windowTools.ExitWindowListener;
import dreamling.objects.AlertTextField;

public class DreamlistMaker extends JFrame {

	private static final long serialVersionUID = 8826901047763564617L;
	private static final String CONFIG_FILE = "dreamlist_maker_config.bin";
	private static final String USED_ACCOUNTS = "used_accounts.txt";
	private Config config;
	private JLabel accountsCheck;
	private JTextArea proxyArea;
	private CustomScrollPane cs;
	private CustomScrollPane accPane;
	private CustomScrollPane tabPane;
	private Tab activeTab;
	
	public class Config extends HashMap<String, String> {
		private static final long serialVersionUID = 1L;
		public ArrayList<Tab> tabs;
		public Config() {
			super();
			tabs = new ArrayList<Tab>();
		}
	}
	
	public class Tab implements Serializable {
		private static final long serialVersionUID = 1L;
		public String name;
		public boolean active;
		public ArrayList<Account> accs;
		public Tab(String name) {
			this.name = name;
			active = false;
			accs = new ArrayList<Account>();
		}
	}
	
	public class Account extends ArrayList<String> {
		private static final long serialVersionUID = 1L;
		public Account() {
			super();
			for (int i = 0; i < 10; i++) {
				this.add("");
			}
		}
	}
	
	public DreamlistMaker() {
		super();
		setTitle("Dreamlist Maker");
		addWindowListener(new ExitWindowListener(new ExitListener(this)));
		setLayout(new FlowLayout());
		loadConfig();
		cs = new CustomScrollPane(true);
		cs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		cs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		cs.setPreferredSize(new Dimension(1265, 500));
		add(cs);
		if (config == null) {
			config = new Config();
		}
		if (config.get("accounts") != null) {
			accountsCheck = new JLabel("Account-file: " + new File(config.get("accounts")).getName());
		} else {
			accountsCheck = new JLabel("Account-file: not found");
		}
		cs.addComponent(accountsCheck);
		
		JButton btnAccounts = new JButton("Select account-file");
		cs.addComponent(btnAccounts);
		JButton btnNextAccounts = new JButton("Set next Accounts");
		cs.addComponent(btnNextAccounts);
		
		cs.newLine();
		JLabel proxyText = new JLabel("Proxies: ");
		cs.addComponent(proxyText);
		proxyArea = new JTextArea("ip:port\nip:port");
		JScrollPane sp = new JScrollPane(proxyArea);
		sp.setPreferredSize(new Dimension(200, 50));
		cs.addComponent(sp);
		JButton btnSetProxies = new JButton("Set Proxies");
		cs.addComponent(btnSetProxies);
		
		cs.newLine();
		JButton btnAdd = new JButton("Add Account");
		cs.addComponent(btnAdd);
		JButton btnGenerate = new JButton("Generate dreamlist into clipboard");
		cs.addComponent(btnGenerate);
		
		cs.newLine();
		accPane = new CustomScrollPane();
		accPane.addComponent(new JLabel("<html><body><b> No Tab selected </b></body></html>"));
		accPane.setPreferredSize(new Dimension(1100, 300));
		cs.addComponent(accPane);
		
		//tab section:
		tabPane = new CustomScrollPane();
		tabPane.setPreferredSize(new Dimension(150, 450));
		JPanel buttonContainer = new JPanel(new BorderLayout());
		JButton btnAddTab = new JButton("Create Tab");
		btnAddTab.setPreferredSize(new Dimension(tabPane.getPreferredSize().width, 20));
		buttonContainer.add(btnAddTab, BorderLayout.NORTH);
		JButton btnDelTab = new JButton("Delete Tab");
		btnDelTab.setPreferredSize(new Dimension(tabPane.getPreferredSize().width, 20));
		buttonContainer.add(btnDelTab, BorderLayout.SOUTH);
		cs.addLeftSidebar(buttonContainer, 0, 1);
		
		if (!config.tabs.isEmpty()) {
			for (Tab tab : config.tabs) { 
				registerTab(tab);
			}
		}
		cs.addLeftSidebar(tabPane, 1, 3);
		
		btnAccounts.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							JFileChooser chooser = new JFileChooser();
							chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
							int returnVal = chooser.showOpenDialog(DreamlistMaker.this);
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								config.put("accounts", chooser.getSelectedFile().getAbsolutePath());
								update();
							}
						}
					});
					t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnNextAccounts.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							if (activeTab != null) {
								for (Account acc : activeTab.accs) {
									if (!acc.get(3).toLowerCase().contains("mule")) { //skip mules
										String login = extractOneAcc();
										if (login != null && !login.isEmpty() && login.split(":").length >= 3) {
											acc.set(0, login.split(":")[0]);
											acc.set(1, login.split(":")[1]);
											acc.set(2, login.split(":")[2]);
										}
									}
								}
								update();
							}
						}
					});
					t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnSetProxies.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							if (activeTab != null) {
								for (int i = 0; i < activeTab.accs.size(); i++) {
									Account acc = activeTab.accs.get(i);
									String proxy = getProxy(i);
									if (proxy != null) {
										acc.set(4, proxy);
									}
								}
								update();
							}
						}
					});
					t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							createAccount();
						}
					});
					t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							String dreamlist = generateDreamlist();
							if (dreamlist.isEmpty()) {
								JOptionPane.showMessageDialog(DreamlistMaker.this, "Error: could not generate Dreamlist.", "Error", JOptionPane.ERROR_MESSAGE);
							} else {
								StringSelection selection = new StringSelection(dreamlist);
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(selection, selection);
								JOptionPane.showMessageDialog(DreamlistMaker.this, "Dreamlist copied to Clipboard.", "Done", JOptionPane.INFORMATION_MESSAGE);
							}
						}
					});
					t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnAddTab.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							String name = JOptionPane.showInputDialog(DreamlistMaker.this, "Name for new Tab: ", "newTab");
							if (name != null && !name.isEmpty()) {
								boolean alreadyExists = false;
								for (Tab tab : config.tabs) {
									if (tab.name.equals(name)) {
										alreadyExists = true;
									}
								}
								if (alreadyExists) {
									JOptionPane.showMessageDialog(DreamlistMaker.this, "Error: Tab name \"" + name + "\" is already taken.", "Error", JOptionPane.ERROR_MESSAGE);
								} else {
									Tab tab = new Tab(name);
									config.tabs.add(tab);
									registerTab(tab);
									activeTab = tab;
									update();
								}
							} else if (name != null && name.isEmpty()){
								JOptionPane.showMessageDialog(DreamlistMaker.this, "Error: Unacceptable Tab name.", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					});
					t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnDelTab.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							if (activeTab == null) {
								JOptionPane.showMessageDialog(DreamlistMaker.this, "Error: No Tab selected to delete.", "Error", JOptionPane.ERROR_MESSAGE);
							} else {
								int res = JOptionPane.showConfirmDialog(DreamlistMaker.this, "Are you sure to delete Tab " + activeTab.name + "?", "Delete", JOptionPane.WARNING_MESSAGE);
								if (res == 0) { //pressed "OK"
									config.tabs.remove(activeTab);
									for (Component tab : tabPane.getContent()) {
										if (tab instanceof JButton) {
											JButton b = (JButton) tab;
											if (b.getText().equals(activeTab.name)) {
												tabPane.removeComponent(b);
											}
										}
									}
									activeTab = null;
									update();
								}
							}
						}
					});
					t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		update();
		pack();
		setVisible(true);
	}
	
	public void registerTab(Tab tab) {
		JButton btnTab = new JButton(tab.name);
		btnTab.setPreferredSize(new Dimension(tabPane.getPreferredSize().width - 3, 30));
		btnTab.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							activeTab = tab;
							update();
						}
					});
					t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		tabPane.newLine();
		tabPane.addComponent(btnTab);
	}
	
	public static void main(String[] args) {
		new DreamlistMaker();
	}
	
	public String generateDreamlist() {
		String result = "";
		final String separation = "~";
		if (activeTab != null) {
			for (Account acc : activeTab.accs) {
				result += acc.get(0) + separation
						+ acc.get(1) + separation
						+ acc.get(2) + separation
						+ acc.get(3) + separation
						+ acc.get(4) + separation
						+ acc.get(5) + separation
						+ acc.get(6) + separation
						+ acc.get(7) + separation
						+ acc.get(8) + separation
						+ acc.get(9) + "\n";
			}
		}
		return result;
	}
	
	public void createAccount() {
		if (activeTab != null) {
			Account acc = new Account();
			final String login = extractOneAcc();
			if (login == null || login.isEmpty() || login.split(":").length < 3) {
				JOptionPane.showMessageDialog(this, "Error: either no Account file found, or wrong format. Expected format: nickname:email:password", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			acc.set(0, login.split(":")[0]);
			acc.set(1, login.split(":")[1]);
			acc.set(2, login.split(":")[2]);
			final String proxy = getProxy(activeTab.accs.size());
			if (proxy != null) {
				acc.set(4, proxy);
			}
			if (!activeTab.accs.isEmpty()) {
				Account toCopy = activeTab.accs.get(activeTab.accs.size() - 1); //last ac
				acc.set(3, toCopy.get(3));
				acc.set(5, toCopy.get(5));
				acc.set(6, toCopy.get(6));
				acc.set(7, toCopy.get(7));
				acc.set(8, toCopy.get(8));
				acc.set(9, toCopy.get(9));
			} else {
				acc.set(7, "9999");
				acc.set(8, "0");
				acc.set(9, "0");
			}
			activeTab.accs.add(acc);
			
			update();
		} else {
			JOptionPane.showMessageDialog(this, "Error: no Tab selected.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//nickname:email:password
	public String extractOneAcc() {
		String result = null;
		String path = config.get("accounts");
		if (path != null) {
			File f = new File(path);
			if (f.exists()) {
				try {
					//read all accounts
					BufferedReader in = new BufferedReader(new FileReader(f));
					List<String> document = new ArrayList<String>();
					for (String line = in.readLine(); line != null; line = in.readLine()) {
						document.add(line);
					}
					result = document.get(0);
					in.close();
					
					//copy first acc and add him to "used" list
					File f2 = new File(USED_ACCOUNTS);
					if (!f2.exists()) {
						f2.createNewFile();
					}
					BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(f2.getName(), true));
					bufferWriter.write(result + "\n");
					bufferWriter.close();
					
					//overwrite all accounts with all-except-first (results in a delete of first acc)
					if (f.exists()) {
						f.delete();
					}
					BufferedWriter bufferWriter2 = new BufferedWriter(new FileWriter(f.getName(), true));
					for (int i = 1; i < document.size(); i++) {
						bufferWriter2.write(document.get(i) + "\n");
					}
					bufferWriter2.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	public String getProxy(int pos) {
		String[] lines = proxyArea.getText().split("\n");
		if (pos < lines.length) {
			if (!lines[pos].isEmpty()) {
				return lines[pos];
			} else {
				System.out.println("empty line at proxies");
			}
		} else {
			System.out.println("not enough proxies specified.");
		}
		return null;
	}
	
	public void update() {
		if (config.get("accounts") != null) {
			accountsCheck.setText("Account-file: " + new File(config.get("accounts")).getName());
		} else {
			accountsCheck.setText("Account-file: not found");
		}
		
		for (Component tab : tabPane.getContent()) {
			if (tab instanceof JButton) {
				JButton b = (JButton) tab;
				if (activeTab != null && b.getText().equals(activeTab.name)) {
					b.setContentAreaFilled(false);
					b.setFocusable(false);
					b.setOpaque(true);
					b.setBackground(Color.LIGHT_GRAY);
				} else {
					b.setContentAreaFilled(false);
					b.setFocusable(false);
					b.setOpaque(true);
					b.setBackground(null);
				}
			}
		}
		
		if (activeTab != null) {
			if (accPane != null) cs.removeComponent(accPane);
			accPane = new CustomScrollPane();
			accPane.setPreferredSize(new Dimension(1100, 300));
			if (activeTab.accs.isEmpty()) {
				accPane.addComponent(new JLabel("<html><body> This Tab has no accounts yet. Click \"Add Account\" </body></html>"));
			} else {
				accPane.addComponent(new JLabel("<html><body> Nickname </body></html>"));
				accPane.addComponent(new JLabel("<html><body> Email </body></html>"));
				accPane.addComponent(new JLabel("<html><body> Password </body></html>"));
				accPane.addComponent(new JLabel("<html><body> Script name </body></html>"));
				accPane.addComponent(new JLabel("<html><body> Proxy:Port </body></html>"));
				accPane.addComponent(new JLabel("<html><body> World </body></html>"));
				accPane.addComponent(new JLabel("<html><body> Script params </body></html>"));
				accPane.addComponent(new JLabel("<html><body> Max minutes </body></html>"));
				accPane.addComponent(new JLabel("<html><body>&nbsp; Max Chunk minutes &nbsp;</body></html>"));
				accPane.addComponent(new JLabel("<html><body>&nbsp; Post-Chunk &nbsp;<br />&nbsp; Break minutes &nbsp;</body></html>"));
				accPane.newLine();
				
				for (Account acc : activeTab.accs) {
					for (int i = 0; i < acc.size(); i++) {
						String data = acc.get(i);
						accPane.addComponent(new AlertTextField(data, acc, i));
					}
					JButton btnDelete = new JButton("Delete");
					accPane.addComponent(btnDelete);
					btnDelete.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							activeTab.accs.remove(acc);
							update();
						}
					});
					accPane.newLine();
				}
			}
			cs.addComponent(accPane);
		}
	}

	public void saveConfig() {
		if (!new File(CONFIG_FILE).exists()) {
			try {
				new File(CONFIG_FILE).createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream fos = new FileOutputStream(new File(CONFIG_FILE));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(config);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean loadConfig() {
		if (new File(CONFIG_FILE).exists()) {
			try {
				FileInputStream fis = new FileInputStream(new File(CONFIG_FILE));
				ObjectInputStream ois = new ObjectInputStream(fis);
				config = (Config) ois.readObject();
				ois.close();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
