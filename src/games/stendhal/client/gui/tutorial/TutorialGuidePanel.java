package games.stendhal.client.gui.tutorial;

import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.gui.styled.WoodStyle;
import games.stendhal.common.TutorialGuideSnapshot;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.DefaultCaret;

/** The guide uses the quiet metal surface and colours of NPC conversations. */
public final class TutorialGuidePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color INK = new Color(212,215,218), MUTED = new Color(174,181,187);
    private static final Color LINE = new Color(88,98,107), ACCENT = new Color(130,174,214);
    private final JLabel title = new JLabel(), progress = new JLabel(), destination = new JLabel();
    private final JPanel body = plain(new BorderLayout(0,10));
    private final JEditorPane instructions = new JEditorPane("text/html", "");
    private final JScrollPane scroll = new JScrollPane(instructions);
    private final JButton fold = button("Zwiń"), help = button("Jak grać?");
    private final Runnable resized;
    private TutorialGuideSnapshot current;
    private boolean helpOpen;

    public TutorialGuidePanel(Runnable close, Runnable resized) {
        this.resized=resized;
        setUI(new BasicPanelUI());setOpaque(false);setLayout(new BorderLayout(0,12));
        setBorder(BorderFactory.createEmptyBorder(20,20,18,20));
        JPanel header=plain(new BorderLayout(8,0));
        title.setFont(new Font("Dialog",Font.BOLD,16));title.setForeground(INK);header.add(title);
        JButton hide=button("x");hide.setToolTipText("Zamknij przewodnik. Otworzysz go ponownie z menu gry.");
        hide.addActionListener(e->close.run());
        JPanel heading=plain(new BorderLayout(0,10));
        JPanel plaque=plain(new BorderLayout(8,0));plaque.setOpaque(true);plaque.setBackground(new Color(34,40,45));
        plaque.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),BorderFactory.createEmptyBorder(6,10,6,6)));
        JLabel section=new JLabel("SAMOUCZEK");section.setFont(new Font("Dialog",Font.BOLD,13));section.setForeground(ACCENT);
        plaque.add(section,BorderLayout.CENTER);plaque.add(hide,BorderLayout.EAST);
        heading.add(plaque,BorderLayout.NORTH);heading.add(header,BorderLayout.SOUTH);add(heading,BorderLayout.NORTH);
        progress.setFont(new Font("Dialog",Font.PLAIN,13));progress.setForeground(MUTED);body.add(progress,BorderLayout.NORTH);
        instructions.setEditable(false);instructions.setFocusable(false);instructions.setOpaque(false);
        instructions.setBorder(BorderFactory.createEmptyBorder());
        ((DefaultCaret)instructions.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        scroll.setBorder(BorderFactory.createEmptyBorder());scroll.setOpaque(false);scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12,0));
        scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){trackColor=new Color(38,43,48);thumbColor=new Color(96,106,116);thumbDarkShadowColor=LINE;thumbLightShadowColor=LINE;thumbHighlightColor=LINE;}
            @Override protected JButton createDecreaseButton(int direction){return arrow(direction);}
            @Override protected JButton createIncreaseButton(int direction){return arrow(direction);}
            private JButton arrow(int direction){return new BasicArrowButton(direction,new Color(45,52,59),LINE,INK,LINE);}
        });
        body.add(scroll,BorderLayout.CENTER);
        destination.setFont(new Font("Dialog",Font.PLAIN,13));destination.setForeground(ACCENT);
        body.add(destination,BorderLayout.SOUTH);add(body,BorderLayout.CENTER);
        JPanel actions=plain(new FlowLayout(FlowLayout.LEFT,0,0));
        fold.addActionListener(e->{body.setVisible(!body.isVisible());fold.setText(body.isVisible()?"Zwiń":"Rozwiń");resized.run();});
        help.addActionListener(e->setHelpOpen(!helpOpen));actions.add(fold);actions.add(Box.createHorizontalStrut(8));actions.add(help);add(actions,BorderLayout.SOUTH);
    }
    public void showStep(TutorialGuideSnapshot snapshot) {
        if(current!=null && current.toWire().equals(snapshot.toWire()))return;
        boolean changed=current==null||!current.step.equals(snapshot.step)||!current.text.equals(snapshot.text)||!current.anchor.equals(snapshot.anchor);
        if(current!=null&&!current.step.equals(snapshot.step)){helpOpen=false;help.setText("Jak grać?");}
        current=snapshot;title.setText(snapshot.title);title.setToolTipText(snapshot.title);
        boolean tips=snapshot.step.startsWith("tips_")||"finished".equals(snapshot.step);
        progress.setText("Ukończone zadania: "+snapshot.completed+" / 4");progress.setVisible(!tips);
        destination.setText(snapshot.x<0?"Menu > Przewodnik":"Cel: "+snapshot.waypoint);
        if(changed)updateText();
    }
    public void expand(){body.setVisible(true);fold.setText("Zwiń");}
    public void setHelpOpen(boolean value){helpOpen=value;expand();help.setText(value?"Wróć do celu":"Jak grać?");updateText();resized.run();}
    public boolean isExpanded(){return body.isVisible();}
    @Override public Dimension getPreferredSize(){return new Dimension(344,body.isVisible()?(helpOpen?470:414):136);}
    private void updateText(){
        if(current==null)return;
        String content="";
        if(current.x>=0)content="<p class='action'>"+escape(current.actionHint())+"</p>";
        content+="<p>"+escape(current.text).replace("\n\n","</p><p>").replace("\n","<br>")+"</p>";
        if(helpOpen)content="<p class='action'>Jak grać?</p>"
            +"<p><b>Ruch</b><br>Kliknij wolne miejsce na mapie albo użyj strzałek.</p>"
            +"<p><b>Rozmowa</b><br>Stań blisko mieszkańca. Kliknij pole czatu na dole, wpisz „cześć” i naciśnij Enter. Potem wybieraj odpowiedzi w oknie rozmowy lub klikaj niebieskie słowa.</p>"
            +"<p><b>Zakupy</b><br>Poproś sprzedawcę o ofertę. Wybierz towar i ilość, sprawdź cenę i potwierdź zakup.</p>"
            +"<p><b>Łupy i zdrowie</b><br>Otwórz zwłoki pokonanego potwora i przenieś łupy do plecaka. Pilnuj paska PZ; jedzenie pomaga odzyskać zdrowie.</p>"
            +"<p><b>Pomoc graczy</b><br>Wpisz na czacie np. <b>/k Gdzie znajdę Mistrza?</b> i naciśnij Enter.</p>";
        instructions.setText("<html><head><style>body{font-family:Dialog;font-size:14px;color:#d4d7da;margin:0;}p{margin:0 0 12px 0;}b{color:#d4d7da}.action{color:#82aed6;font-weight:bold}hr{color:#58626b}</style></head><body>"+content+"</body></html>");
        instructions.setCaretPosition(0);
    }
    private static String escape(String value){return value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");}
    private static JPanel plain(LayoutManager layout){JPanel p=new JPanel(layout);p.setUI(new BasicPanelUI());p.setOpaque(false);p.setBorder(BorderFactory.createEmptyBorder());return p;}
    private static JButton button(String label){
        JButton b=new JButton(label);b.setUI(new BasicButtonUI());b.setFocusable(false);b.setFont(new Font("Dialog",Font.PLAIN,13));
        b.setForeground(INK);b.setBackground(new Color(45,52,59));b.setOpaque(true);b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),BorderFactory.createEmptyBorder(5,9,5,9)));return b;
    }
    @Override protected void paintComponent(Graphics graphics){
        StyleUtil.fillBackground(WoodStyle.getInstance(),graphics,0,0,getWidth(),getHeight());
        Graphics2D g=(Graphics2D)graphics.create();g.setColor(new Color(38,43,48,242));
        g.fillRect(10,10,getWidth()-20,getHeight()-20);g.setColor(LINE);g.drawRect(10,10,getWidth()-21,getHeight()-21);g.dispose();
        super.paintComponent(graphics);
    }
}
