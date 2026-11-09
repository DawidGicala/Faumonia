package games.stendhal.server.core.rp.quest;

/** Short introductions; actual requirements and completion remain in the original stage actions. */
final class MasterStory {
    private static final String[] INTRO={"",
        "Zacznij od krótkiej wyprawy przeciw szczurom. Nauczysz się obserwować przeciwnika i wracać po nagrodę, kiedy cel jest wykonany.",
        "Dobry wojownik potrafi pomóc mieszkańcom i rozsądnie handlować. Sprawdzimy twoje przysługi dla Soro, Jenny i Nishiyi, a potem omówimy zakup potrzebnego przedmiotu.",
        "Odwaga przydaje się również poza walką. Sprawdzimy pomoc Sedro i twoje poznanie mieszkańców, zanim powierzysz mi kolejne trofeum.",
        "Pora zasłużyć na zaufanie Semos. Omówimy zadanie, które pozwoli ci wykazać się wobec miasta i zdobyć następny stopień.",
        "Rzemieślnik i głodny mieszkaniec też potrzebują wojownika. Zacznij od Ouchita i Joshuy; wykonane przysługi otworzą dalszą część próby.",
        "Twoja droga prowadzi poza Semos. Sprawdzimy zasługi dla Fado i sprawę sztyletu żywiołów, a potem przygotowanie do kolejnej wyprawy.",
        "Kirdneh potrzebuje ludzi, którym można zaufać. Zdobyte tam uznanie będzie podstawą tej próby; kolejne wymaganie omówimy po powrocie.",
        "O sile wyprawy decydują także zapasy. Pomóż Phalkowi, a następnie wróć po dalsze wskazówki dotyczące tego stopnia.",
        "Za tobą pierwsze starcia, przed tobą odpowiedzialność za dalsze miasta. Sprawdzimy twoje polowania i zasługi dla Nalwor.",
        "Odnajdź siedem aniołków, a potem poznaj żywioły na dzikiej wyspie i licha w podziemiach zamku Orril. Przed polowaniem podam dokładne cele; licznik pokaże, ile walk zostało.",
        "Nie każda przysługa kończy się na polowaniu. Pomoc Bario jest częścią tej próby; pozostałe warunki poznasz przed przyjęciem zadania.",
        "Ados i wyprawy przeciw krasnoludom sprawdzą twoją wytrwałość. Najpierw omówimy wykonane zadania, potem potrzebny przedmiot.",
        "Dobry ekwipunek idzie w parze z doświadczeniem. Sprawdzimy kolekcję broni i pokonanych przeciwników, zanim ruszysz po kolejny stopień.",
        "Podziemia Athor wymagają opanowania. Pomoc w ich oczyszczeniu oraz walka z albinosami przygotują cię do dalszej części próby.",
        "Smocza jaskinia i olbrzymy nie wybaczają pośpiechu. Wróć z ukończonymi zadaniami, a omówimy dowód, który zakończy ten etap.",
        "Przyjrzymy się twoim wyprawom przeciw barbarzyńcom i zadaniu Woda. Liczą się zakończone zobowiązania, nie same obietnice.",
        "Staro w Fado przybliży ci magię, a zadanie Hogarta zaprowadzi cię do wampirów w katakumbach Semos. Po wykonaniu obu przysług porozmawiamy o wielkich eliksirach, które Dane Taker przygotowuje z wampirzego truchła.",
        "Doświadczenie powinno służyć również tym, którzy sami nie mogą się obronić. Omówimy walkę z Madaramem i pomoc uwięzionemu krasnoludowi.",
        "Xenos to kolejny sprawdzian przygotowania. Wykonaj powiązane zadania i wróć; dalszy cel otrzymasz w tej samej rozmowie.",
        "Tunele chaosu wymagają odwagi i rozsądku. Sprawdzimy twoje zasługi oraz pokonanych przeciwników, zanim przyznam kolejny stopień.",
        "Czas połączyć doświadczenia z kilku wypraw. Kolekcja płaszczy, sprawa Verdana i smocza kryjówka składają się na tę próbę.",
        "Broń i doświadczenie z krypty muszą iść w parze. Sprawdzimy sprawę miecza nieśmiertelnych i zadanie Lucka, a potem twoje polowanie.",
        "Pomoc Waltowi i wyprawa dla Sausida mają wspólny cel: ograniczyć zagrożenie dla mieszkańców. Po ich wykonaniu przyniesiesz mi wskazaną ilość gruczołów przędzy.",
        "Odnalezienie smoków i druga kolekcja płaszczy świadczą o dalekiej drodze. Ostatnim dowodem w tym etapie będą wskazane smocze pazury."};
    private static final String[] COMPLETED_SUBJECTS = {"", "szczurów w Semos", "pomocy Soro, Jenny i Nishiyi", "pomocy Sedro i poznawania mieszkańców", "zasług dla Semos", "przysług dla Ouchita i Joshuy", "Fado i sztyletu żywiołów", "uznania zdobytego w Kirdneh", "zapasów i ubrań dla Phalka", "polowań i zasług dla Nalwor", "siedmiu aniołków, żywiołów i lichów", "pomocy marznącemu Bario", "Ados i wypraw przeciw krasnoludom", "kolekcji broni i walki z potworami", "podziemi Athor i albinosów", "smoczej jaskini i olbrzymów", "barbarzyńców i pomocy Wodowi", "zadań Staro i Hogarta oraz wielkich eliksirów", "Madarama i uwięzionego krasnoluda", "wyprawy do Xenos", "tuneli chaosu", "kolekcji płaszczy, Verdana i smoczej kryjówki", "miecza nieśmiertelnych i wyprawy dla Lucka", "Walta, Sausida i gruczołów przędzy", "poszukiwania smoków, drugiej kolekcji płaszczy i smoczych pazurów"};
    static String completed(int stage) { return "Próba dotycząca " + COMPLETED_SUBJECTS[stage] + " jest już za tobą. Nagroda została przekazana. Wybierz kolejny dostępny etap."; }
    static String introduction(int stage){return INTRO[stage];}
    static String clean(String text){
        if(text==null)return null;
        return text.replaceAll("Mistrz(a|owi|em)? Faumonii [IVX]+", "Mistrz$1 Faumonii")
            .replace("Widze", "Widzę").replace("Nastepnie", "Następnie")
            .replace("nastepne", "następne").replace("musze", "muszę")
            .replace("mieszkancom", "mieszkańcom").replace("cie ","cię ")
            .replace('\u2014','-').replace('\u2013','-');
    }
}
