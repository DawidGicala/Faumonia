package games.stendhal.client.gui.tutorial;

import games.stendhal.common.TutorialGuideSnapshot;
import java.awt.*;

/** Readable map instructions, drawn without capturing clicks. */
public final class TutorialGuideMarker {
    private TutorialGuideMarker(){}
    public static void paint(Graphics2D graphics,TutorialGuideSnapshot s,String zone,int x,int y,int width,int height){
        if(s==null||s.x<0||!s.zone.equals(zone)||width<180||height<100||s.isConversationAction())return;
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Dialog",Font.PLAIN,13));FontMetrics fm=g.getFontMetrics();
        String action=s.actionHint(),name=s.waypoint;
        int boxWidth=Math.min(width-16,Math.max(fm.stringWidth(action),fm.stringWidth(name))+24),boxHeight=49;
        int left=Math.max(8,Math.min(width-boxWidth-8,x-boxWidth/2));
        int top=Math.max(8,Math.min(height-boxHeight-16,y-103));
        g.setColor(new Color(38,43,48,246));g.fillRoundRect(left,top,boxWidth,boxHeight,8,8);
        g.setColor(new Color(88,98,107));g.drawRoundRect(left,top,boxWidth,boxHeight,8,8);
        g.setColor(new Color(174,181,187));g.drawString(shorten(name,fm,boxWidth-22),left+11,top+18);
        g.setColor(new Color(130,174,214));g.drawString(shorten(action,fm,boxWidth-22),left+11,top+37);
        boolean onScreen=x>=8&&x<width-8&&y>=8&&y<height-8;
        if(onScreen){int point=Math.max(left+12,Math.min(left+boxWidth-12,x));
            g.fillPolygon(new int[]{point-5,point+5,point},new int[]{top+boxHeight+3,top+boxHeight+3,top+boxHeight+9},3);
        }else{
            int cx=Math.max(7,Math.min(width-7,x)),cy=Math.max(7,Math.min(height-7,y));
            g.translate(cx,cy);g.rotate(Math.atan2(y-cy,x-cx));
            g.fillPolygon(new int[]{6,-5,-2,-5},new int[]{0,-5,0,5},4);
        }
        g.dispose();
    }
    private static String shorten(String value,FontMetrics fm,int width){
        if(fm.stringWidth(value)<=width)return value;
        while(!value.isEmpty()&&fm.stringWidth(value+"...")>width)value=value.substring(0,value.length()-1);
        return value+"...";
    }
}
