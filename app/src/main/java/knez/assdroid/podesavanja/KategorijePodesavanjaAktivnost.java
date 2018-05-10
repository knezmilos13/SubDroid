package knez.assdroid.podesavanja;

import knez.assdroid.R;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class KategorijePodesavanjaAktivnost extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		namestiAdapter();
	}

	private void namestiAdapter() {
		Resources res = getResources();
		String[] kategorije = { res.getString(R.string.podesavanja_global),
				res.getString(R.string.podesavanja_editor),
				res.getString(R.string.podesavanja_prevodilac)};
		getListView().setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, kategorije));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent namera = new Intent();
		switch (position) {
		case 0:
			namera.setClass(this, PodesavanjaEditorAktivnost.class);
			break;
		case 1:
			namera.setClass(this, PodesavanjaPrevodilacAktivnost.class);
			break;
		default:
			return;
		}
		startActivity(namera);
	}

}
