package games.stendhal.client.gui.progress;

import games.stendhal.common.QuestProgress;
import games.stendhal.common.QuestProgress.Objective;

/** Swing HTML fragment shared with the local preview and rendering tests. */
public final class QuestProgressFormatter {
    private QuestProgressFormatter() { }

    public static String render(QuestProgress progress) {
        if (progress == null) { return ""; }
        String status;
        switch (progress.getState()) {
        case READY_TO_DELIVER: status = "Gotowe do oddania"; break;
        case COMPLETED: status = "Ukończone"; break;
        case DECLINED: status = "Odmówiono pomocy"; break;
        case UNKNOWN: status = "Postęp wymaga sprawdzenia"; break;
        default: status = "W toku";
        }
        StringBuilder html = new StringBuilder("<h3>Bieżący cel</h3><p><b>");
        html.append(status).append("</b></p>");
        for (Objective objective : progress.getObjectives()) {
            html.append("<p>").append(escape(objective.getLabel())).append(": <b>")
                .append(objective.getCurrent()).append(" / ").append(objective.getRequired())
                .append("</b>");
            if (objective.getCarried() >= 0) {
                html.append(" &nbsp; (przy sobie: ").append(objective.getCarried()).append(")");
            }
            html.append("</p>");
        }
        if (!progress.getNextNPC().isEmpty()) {
            html.append("<p><b>Rozmówca:</b> ").append(escape(progress.getNextNPC())).append("</p>");
        }
        html.append("<p>").append(escape(progress.getNextStep())).append("</p><hr>");
        return html.toString();
    }

    public static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
