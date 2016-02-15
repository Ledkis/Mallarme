package ledkis.module.mallarme;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ledkis on 15/02/2016.
 */
public class MallarmeTextManager {

    public static final String TAG = "MallarmeTextManager";

    public static final String TEXT_SIZE_DELIMITER = "--";
    public static final int DEFAULT_VERSE_SIZE = 25;

    private Context context;

    private String poemRawText;

    private ArrayList<VerseGroup> verseGroups;

    private int verseNbr;



    public MallarmeTextManager(Context context, String textAssetsPath, boolean shuffleVerses) {
        this.context = context;

        poemRawText = Utils.valueOrDefault(Utils.readTextFileFromAssets(context, textAssetsPath), "");
        poemRawText = poemRawText.trim().replaceAll("\\n\\n\\n+", "\n\n");

        String[] verseGroupTexts = poemRawText.split("\\n\\n");

        verseGroups = new ArrayList<>();
        for(String verseGroupText : verseGroupTexts){

            String[] verseTexts = verseGroupText.split("\\n");

            String firstLine = verseTexts[0];

            VerseGroup verseGroup;
            if(firstLine.split(TEXT_SIZE_DELIMITER).length > 1){
                int textSize = Integer.parseInt(firstLine.split(TEXT_SIZE_DELIMITER)[1]);
                verseGroup = new VerseGroup(Arrays.copyOfRange(verseTexts, 1, verseTexts.length), textSize, shuffleVerses);
            } else {
                verseGroup = new VerseGroup(Arrays.copyOfRange(verseTexts, 0, verseTexts.length), 16, shuffleVerses);
            }

            verseGroups.add(verseGroup);
        }

        for(VerseGroup verseGroup : verseGroups){
            verseNbr += verseGroup.getVerses().length;
        }
    }


    public String getVerse(int verseIndex){
        verseIndex = verseIndex - 1;
        verseIndex = verseIndex%this.verseNbr;

        String verse = "";

        int verseCount = 0;
        for(VerseGroup verseGroup : verseGroups){
            if((verseGroup.getVerses().length + verseCount) > verseIndex){
                verseIndex = verseIndex - verseCount;
                verse = verseGroup.getVerses()[verseIndex];
                break;
            } else {
                verseCount += verseGroup.getVerses().length;
            }
        }

        return verse;

    }

    public int getVerseSize(int verseIndex){
        verseIndex = verseIndex - 1;
        verseIndex = verseIndex%this.verseNbr;

        int verseCount = 0;
        for(VerseGroup verseGroup : verseGroups){
            if((verseGroup.getVerses().length + verseCount) > verseIndex){
                return verseGroup.getVersesSize();
            } else {
                verseCount += verseGroup.getVerses().length;
            }
        }

        return DEFAULT_VERSE_SIZE;
    }


    public class VerseGroup {

        private String[] verses;

        private int versesSize;

        private boolean shuffleVerses;

        public VerseGroup(String[] verses, int versesSize, boolean shuffleVerses) {
            this.verses = verses;
            this.versesSize = versesSize;
            this.shuffleVerses = shuffleVerses;

            if(shuffleVerses)
                Utils.shuffleArray(verses);
        }

        public String[] getVerses() {
            return verses;
        }

        public int getVersesSize() {
            return versesSize;
        }

        public boolean isShuffleVerses() {
            return shuffleVerses;
        }
    }


}
