package de.freddi.android.lostwords;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by freddi on 27.03.2016.
 */
class FavoriteHandler {
    private final FloatingActionButton m_fabFav;
    private final Set<String> m_setFavs = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public FavoriteHandler(FloatingActionButton fabFav, final Set<String> setFavs) {
        m_fabFav = fabFav;
        m_setFavs.addAll(setFavs);
    }

    public boolean checkFavorite(final LostWord lw) {
        if (m_setFavs.contains(lw.getWord()) ) {
            /** ist in der Liste --> Button hat Entfernen Funktionalität */
            m_fabFav.setImageResource(android.R.drawable.btn_star_big_off);
            return true;
        } else {
            /** ist nicht in der Liste --> Button hat Hinzufügen Funktionalität */
            m_fabFav.setImageResource(android.R.drawable.btn_star_big_on);
            return false;
        }
    }

    public String handleFavoriteFloatbuttonClick(final LostWord lw, final Resources res) {
        String strReturn;
        final boolean bIsPresent = checkFavorite(lw);
        if (bIsPresent) {
            strReturn = removeFromFavorites(lw, res);
        } else {
            strReturn = addToFavorites(lw, res);
        }

        /** nach der Änderung: Nochmal checken, dadurch wird der Floatbutton aktualisiert */
        checkFavorite(lw);
        return strReturn;
    }

    private String addToFavorites(final LostWord lw, final Resources res) {
        m_setFavs.add(lw.getWord());
        return res.getString(R.string.favorites_add, lw.getWord());
    }

    private String removeFromFavorites(final LostWord lw, final Resources res) {
        Iterator<String> iterFavs = m_setFavs.iterator();
        while (iterFavs.hasNext()) {
            if (iterFavs.next().equals(lw.getWord())) {
                iterFavs.remove();
            }
        }

        return res.getString(R.string.favorites_remove, lw.getWord());
    }

    /** Anzeige im Menü */
    public Set<String> getFavorites() {
        return this.m_setFavs;
    }

    public void settingsPersistFavorites(SharedPreferences prefs, final String strSettingsKey, final View v) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(strSettingsKey, m_setFavs);

        if (!editor.commit()) {
            Helper.showSnackbar("Problem beim Speichern der Favoriten", v, Snackbar.LENGTH_SHORT);
        }
    }
}
