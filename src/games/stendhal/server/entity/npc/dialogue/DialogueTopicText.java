package games.stendhal.server.entity.npc.dialogue;

/** Short returns to conversation, grounded in each speaker's existing work. */
final class DialogueTopicText {
    private DialogueTopicText() { }
    static String forNpc(String name) {
        switch (name) {
            case "Josephine": return "O jakich płaszczach chcesz porozmawiać? Mogę też opowiedzieć o swojej kolekcji.";
            case "Bario": return "Chłód mi nie odpuszcza, ale na rozmowę znajdę chwilę. Co cię sprowadza?";
            case "Adalbert": return "Słucham. Jeśli masz jeszcze jakieś pytanie, postaram się odpowiedzieć.";
            case "Carena": return "Zostań jeszcze chwilę. O czym chcesz porozmawiać?";
            case "Ivan Abe": return "Słucham. Mam nadzieję, że chodzi o coś godnego przyszłego króla!";
            case "Kendra Mattori": return "W czym jeszcze mogę pomóc?";
            case "Alrak": return "Odłożę na chwilę narzędzia. Czego chcesz się dowiedzieć?";
            case "Baldemar": case "Hogart": case "Ludwik": return "Słucham. Chodzi o pracę w kuźni czy o coś innego?";
            case "Samaron": return "Co jeszcze chcesz wiedzieć o mojej pracy?";
            case "Woy'tyk": return "Słucham. Co jeszcze chcesz wiedzieć o Athor?";
            case "Kolekcjoner Andrzej": case "Wandy": return "Chętnie opowiem o swoich zbiorach. Co cię interesuje?";
            case "Phalk": return "Masz jakieś pytanie o posterunek albo zapasy?";
            case "Fag": return "Słucham. O którym wyzwaniu chcesz porozmawiać?";
            case "Jenny": return "Chodzi o polowanie czy o mielenie zboża?";
            default: return "Słucham. O co chcesz zapytać?";
        }
    }
}
