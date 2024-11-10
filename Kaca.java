package Kaca;


import java.awt.*;
import java.awt.event.*;
import static java.lang.String.format;
import java.util.*;
import java.util.List;
import javax.swing.*;


/**
 * Vbistvu bo ta komentar zamenjal README (saj Git in eclipe nista zelo kompatibilna):
 * Igrica Kaèa v bistvu nama je v izpitnem èasu zmankalo èasa, da bi uredila izbiro hitrosti.
 * 
 * @author Matej/Peter
 *
 */
public class Kaca extends JPanel implements Runnable {
	   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static void main(String[] args) {
		      SwingUtilities.invokeLater(
		         () -> {
		            JFrame glavniZaslon = new JFrame();
		            glavniZaslon.setResizable(false);
		            glavniZaslon.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		            glavniZaslon.setTitle("Kaèa");
		            glavniZaslon.add(new Kaca(), BorderLayout.CENTER);
		            glavniZaslon.pack();
		            glavniZaslon.setLocationRelativeTo(null);
		            glavniZaslon.setVisible(true);
		            
			         });
			   }
	
	/**
	 * Smer samo shranjuje "smerni vektor" zato da se pozicija primerno spreminja.
	 * 
	 */
   enum Smer {
      gor(0, -1), desno(1, 0), dol(0, 1), levo(-1, 0);
 
      Smer(int x, int y) {
         this.x = x; this.y = y;
      }
 
      final int x, y;
   }
 

   static final int ZID = -1;

 
   volatile boolean konec = true;
 
   Thread igrMis;
   int tocke = 0;
   int nVrstic = 50;
   int nStolpcov = 50;
   Smer kam;
 
   int[][] polje;
   List<Point> kaca, mis;
   Font smallFont;
	/**
	 * Kaca v bistvu ustvari polje in skrbi za sam potek igre.
	 * 
	 */
   public Kaca() {
      setPreferredSize(new Dimension(500, 500));
      setBackground(Color.WHITE);
      setFont(new Font("Ariel", Font.BOLD, 48));
      setFocusable(true);
 
      smallFont = getFont().deriveFont(Font.BOLD, 18);
      initPolje();
 
      addMouseListener(
         new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               if (konec) {
            	   novaIgra();
                  repaint();
               }
            }
         });
 
      addKeyListener(
         new KeyAdapter() {
 
            @Override
            public void keyPressed(KeyEvent tipka) {
 
               switch (tipka.getKeyCode()) {
 
                  case KeyEvent.VK_UP:
                     if (kam != Smer.dol)
                        kam = Smer.gor;
                     break;
 
                  case KeyEvent.VK_LEFT:
                     if (kam != Smer.desno)
                        kam = Smer.levo;
                     break;
 
                  case KeyEvent.VK_RIGHT:
                     if (kam != Smer.levo)
                        kam = Smer.desno;
                     break;
 
                  case KeyEvent.VK_DOWN:
                     if (kam != Smer.gor)
                        kam = Smer.dol;
                     break;
               }
               repaint();
            }
         });
   }
 
	/**
	 * novaIgra poskrbi da ko se zaletimo bodisi v zid bodisi v kaèo
	 * zaène novo igro. 
	 */
   void novaIgra() {
      konec = false;
 
      stop();
      initPolje();
      mis = new LinkedList<>();
 
      kam = Smer.desno;

      kaca = new ArrayList<>();
      for (int x = 0; x < 1; x++)
         kaca.add(new Point(nStolpcov / 2 + x, nVrstic / 2));
 
      do
         dodajMis();
      while(mis.isEmpty());
 
      (igrMis = new Thread(this)).start();
   }
 
   void stop() {
      if (igrMis != null) {
         Thread mis = igrMis;
         igrMis = null;
         mis.interrupt();
      }
   }
	/**
	 * postavi zidove na prava mesta 
	 */
   void initPolje() {
      polje = new int[nVrstic][nStolpcov];
      for (int r = 0; r < nVrstic; r++) {
         for (int c = 0; c < nStolpcov; c++) {
            if (c == 0 || c == nStolpcov - 1 || r == 0 || r == nVrstic - 1)
               polje[r][c] = ZID;
         }
      }
   }
	/**
	 *run skrbi za premik in hitrost kaèe.
	 */
   @Override
   public void run() {
 
      while (Thread.currentThread() == igrMis) {
 
         try {
            Thread.sleep(Math.max(100 - tocke, 25));
         } catch (InterruptedException e) {
            return;
         }
 
         if (hitsZid() || hitsKaca()) {
        	 konecIgre();
         } else {
            if (pojeMis()) {
               tocke++;
               povecaKaca();
            }
            premikKace();
            
         }
         repaint();
      }
   }
	/**
	 * preveri ali smo zadeli zid
	 * 
	 */
   boolean hitsZid() {
      Point glava = kaca.get(0);
      int novSto = glava.x + kam.x;
      int novVrs = glava.y + kam.y;
      return polje[novVrs][novSto] == ZID;
   }
	/**
	 * preveri ali smo zadeli rep kaèe
	 * 
	 */
   boolean hitsKaca() {
      Point glava = kaca.get(0);
      int novSto = glava.x + kam.x;
      int novVrs = glava.y + kam.y;
      for (Point tocka : kaca)
         if (tocka.x == novSto && tocka.y == novVrs)
            return true;
      return false;
   }
	/**
	 * preveri ali smo pobrali miš in se zato doda krogec {@link povecaKaca}
	 * 
	 */
   boolean pojeMis() {
	  dodajMis();
      Point glava = kaca.get(0);
      int novSto = glava.x + kam.x;
      int novVrs = glava.y + kam.y;
      for (Point tocka : mis)
         if (tocka.x == novSto && tocka.y == novVrs) {
            return mis.remove(tocka);
         }
      return false;
   }
 
   void konecIgre() {
      konec = true;
      stop();
   }
	/**
	 * skrbi za pozicijo kaèe
	 * 
	 */
   void premikKace() {
      for (int i = kaca.size() - 1; i > 0; i--) {
         Point tocka1 = kaca.get(i - 1);
         Point tocka2 = kaca.get(i);
         tocka2.x = tocka1.x;
         tocka2.y = tocka1.y;
      }
      Point glava = kaca.get(0);
      glava.x += kam.x;
      glava.y += kam.y;
   }
	/**
	 * ta pa dobesedno poveèa kaèo
	 * 
	 */
   void povecaKaca() {
      Point rep = kaca.get(kaca.size() - 1);
      int x = rep.x + kam.x;
      int y = rep.y + kam.y;
      kaca.add(new Point(x, y));
   }
   /**
	 * na prosto mesto doda niš 
	 */
   void dodajMis() {
      if (mis.size() < 1) {
    	  Random rand = new Random();
 
         if (rand.nextInt(10) == 0) { 
 
            if (rand.nextInt(4) != 0) {  
               int x, y;
               while (true) {
 
                  x = rand.nextInt(nStolpcov);
                  y = rand.nextInt(nVrstic);
                  if (polje[y][x] != 0)
                     continue;
 
                  Point tocka = new Point(x, y);
                  if (kaca.contains(tocka) || mis.contains(tocka))
                     continue;
 
                  mis.add(tocka);
                  break;
               }
            } else if (mis.size() > 1)
               mis.remove(0);
         }
      }
   }
   
   void narisiPolje(Graphics2D plosca) {
      plosca.setColor(Color.black);
      for (int r = 0; r < nVrstic; r++) {
         for (int c = 0; c < nStolpcov; c++) {
            if (polje[r][c] == ZID)
               plosca.fillRect(c * 10, r * 10, 10, 10);
         }
      }
   }
 
   void narisiKaca(Graphics2D kam) {
      kam.setColor(Color.green);
      for (Point tocka : kaca)
         kam.drawOval(tocka.x * 10, tocka.y * 10, 10, 10);
 
      Point glava = kaca.get(0);
      kam.drawOval(glava.x * 10, glava.y * 10, 10, 10);
   }
 
   void narisiMis(Graphics2D kam) {
      kam.setColor(Color.red);
      for (Point tocka : mis)
         kam.fillRect(tocka.x * 10, tocka.y * 10, 10, 10);
   }
 
   void narisiZaslon(Graphics2D kam) {
      kam.setColor(Color.blue);
      kam.setFont(getFont());
      kam.drawString(" KAÈA", 155, 210);
	  kam.setColor(Color.black);
	  kam.setFont(smallFont);
	  kam.drawString(" Pritsni za zaèetek", 160, 240);
   }
 
   void izpisTock(Graphics2D kam) {
      int h = getHeight();
      kam.setFont(smallFont);
      kam.setColor(getForeground());
      String besedilo = format("Toèke: %d", tocke);
      kam.drawString(besedilo, 30, h - 30);

   }
 
   @Override
   public void paintComponent(Graphics slika) {
      super.paintComponent(slika);
      Graphics2D nova = (Graphics2D) slika;
      nova.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
             RenderingHints.VALUE_ANTIALIAS_ON);
 
      narisiPolje(nova);
 
      if (konec) {
    	  narisiZaslon(nova);
      } else {
    	  narisiKaca(nova);
    	  narisiMis(nova);
    	  izpisTock(nova);
      }
   }
 

	
}