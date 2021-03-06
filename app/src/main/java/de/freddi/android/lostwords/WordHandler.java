package de.freddi.android.lostwords;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by freddi on 27.03.2016.
 */
class WordHandler {

    private final Random m_rnd = new Random(System.nanoTime());
    private final ContentResolver m_resolver;

    private int m_nCachedCount = 0;
    private LostWord m_CachedWord = null;

    /** zieht die Liste aus den String Ressourcen (strings.xml) */
    public WordHandler(final String[] strArrWords, ContentResolver resolver) {
        m_resolver = resolver;

        /**
         * sicherheithalber: Wörterliste IMMER alphabetisch sortieren, wer weiss
         * wie die aus den Ressourcen zurückkommen und dann im ContentProvider
         * landen
         */
        Set<String> setWords = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Collections.addAll(setWords, strArrWords);

        int nIdxDash, nID = 0;
        ContentValues values;
        for (String setWord: setWords) {
            nIdxDash = setWord.indexOf(" - ");

            if (nIdxDash != -1) {
                values = new ContentValues();
                values.put(BaseColumns._ID, String.valueOf(nID++));
                values.put(SearchManager.SUGGEST_COLUMN_TEXT_1, setWord.substring(0, nIdxDash).trim());
                values.put(SearchManager.SUGGEST_COLUMN_TEXT_2, setWord.substring(nIdxDash + 3).trim());

                m_resolver.insert(WordContentProvider.CONTENT_URI, values);
                m_nCachedCount++;
            }
        }
    }

    /**
     * move through the words via next, previous or random
     * 
     * @param i NEXT, PREV or RANDOM
     */
    public void generateNewPosition(final IndexType i) {
        int nID = 0;
        if (m_CachedWord != null) {
            nID = m_CachedWord.getID();
        }
        if (i == IndexType.NEXT) {
            nID++;
        } else if (i == IndexType.PREV) {
            nID--;
        } else {
            /** RANDOM */
            nID = m_rnd.nextInt(m_nCachedCount - 1);
        }

        /** Umlauf in beide Richtungen */
        if (nID < 0) {
            nID = m_nCachedCount - 1;
        } else if (nID >= m_nCachedCount) {
            nID = 0;
        }

        selectWordByID(nID);
    }

    /**
     * wordcount
     * 
     * @return
     */
    public int getWordCount() {
        return m_nCachedCount;
    }

    /**
     * current word
     * 
     * @return
     */
    public LostWord getCurrentWord() {
        return m_CachedWord;
    }

    /**
     * makes the word identified by the id the current word
     * use by the buttons/swipes, random (doubletap, shake)
     * 
     * @param nID
     */
    private void selectWordByID(final int nID) {
        Cursor cursor = m_resolver.query(
                WordContentProvider.CONTENT_URI,
                null,
                SelectionType.ID.name(),
                new String[]{String.valueOf(nID)},
                null);

        if (cursor != null && cursor.moveToFirst()) {
            m_CachedWord = new LostWord(
                nID,
                cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2))
            );

            cursor.close();
        }
    }

    /**
     * makes the word identified by "strWord" the current word.
     * used from the serach view and favorites
     * 
     * @param strWord
     */
    public void selectWordByString(final String strWord) {
        Cursor cursor = m_resolver.query(
                WordContentProvider.CONTENT_URI,
                null,
                SelectionType.WORD.name(),
                new String[]{strWord},
                null);

        if (cursor != null && cursor.moveToFirst()) {
            m_CachedWord = new LostWord(
                    cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2))
            );

            cursor.close();
        }
    }
}
