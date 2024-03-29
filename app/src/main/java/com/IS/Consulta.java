package com.IS;

import java.util.ArrayList;

import model.Imovel;
import util.Constantes;
import views.MainTab;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import business.ControladorImovel;
import business.ControladorRota;

public class Consulta extends ListActivityLog {
	
	MySimpleArrayAdapter enderecoList;
	ArrayList<String> listStatusImoveis;
	ArrayList<Imovel> listImoveis;
	Spinner spinnerMetodoBusca;
	Spinner spinnerFiltro;
	String searchCondition = null;
	static int metodoBusca = Constantes.METODO_BUSCA_MATRICULA;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.consulta);
 
    	metodoBusca = Constantes.METODO_BUSCA_MATRICULA;
    	
    	// Spinner Metodo Busca
    	spinnerMetodoBusca = (Spinner) findViewById(R.id.spinnerMetodoBusca);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.consulta, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMetodoBusca.setAdapter(adapter);
        metodoBuscaOnItemSelectedListener(spinnerMetodoBusca);
        
        // Deixa o fundo da lista transparente quando feito o scroll
        this.getListView().setCacheColorHint(0);
        
        // Button Consulta 
        final Button buttonConsulta = (Button)findViewById(R.id.buttonConsulta);
        buttonConsulta.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	loadEnderecoImoveis();
            }
        });
    	
    }
    
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);//must store the new intent unless getIntent() will return the old one.
//		loadEnderecoImoveis();
	}

    private void loadEnderecoImoveis(){

    	enderecoList = null;
    	setListAdapter(enderecoList);

    		if (ControladorRota.getInstancia().getDataManipulator() != null){
    			
    			searchCondition = null;

    			String valorBusca = "\"" + ((EditText)findViewById(R.id.consulta)).getText().toString() + "\"";
    			
    			// Verifica Método de Busca				
    			if (metodoBusca == Constantes.METODO_BUSCA_HIDROMETRO){
	    			
	    			// Busca a matricula do imovel pelo numero do hidrômetro.
	    			valorBusca = String.valueOf(ControladorRota.getInstancia().getDataManipulator().selectMatriculaMedidor("numero_hidrometro = " + valorBusca + " COLLATE NOCASE"));
	    			
	    			searchCondition = "(matricula = " + valorBusca + ")";	    				

	    		} else if (metodoBusca == Constantes.METODO_BUSCA_MATRICULA){
	    			searchCondition = "(matricula = " + valorBusca + ")";	    				

    			} else if (metodoBusca == Constantes.METODO_SEQUENCIAL_ROTA) {
    				Log.i("VALOR BUSCA", valorBusca);
    				searchCondition = String.format("(sequencial_rota = %s)", valorBusca);

    			} else if (metodoBusca == Constantes.METODO_SEQUENCIAL) {
    				searchCondition = String.format("(id = %s)", valorBusca);

    			} else if (metodoBusca == Constantes.METODO_QUADRA) {
    				searchCondition = String.format("(quadra = %s)", valorBusca);
    			}

	    		Log.i("FILTRO", searchCondition);
	    		
	        	listStatusImoveis = (ArrayList)ControladorRota.getInstancia().getDataManipulator().selectStatusImoveis(searchCondition);
	    		
    	    	ArrayList<String> listEnderecoImoveis = (ArrayList)ControladorRota.getInstancia().getDataManipulator().selectEnderecoImoveis(searchCondition);

    	    	if(listEnderecoImoveis != null && listEnderecoImoveis.size() > 0){
    	        	enderecoList = new MySimpleArrayAdapter(this, listEnderecoImoveis);
    	        	setListAdapter(enderecoList);
    	        	ListaImoveis.tamanhoListaImoveis = ControladorRota.getInstancia().getDataManipulator().getNumeroImoveis();
    	    	}
    		}
    	}
    
	@Override
	protected void onListItemClick(ListView l, View view, int position, long id) {
		// user clicked a list item, make it "selected"
		enderecoList.setSelectedPosition(position);

		ControladorImovel.getInstancia().setImovelSelecionadoByListPositionInConsulta(position, searchCondition);
		Intent myIntent = new Intent(getApplicationContext(), MainTab.class);
		startActivityForResult(myIntent, 0);
	}
	
	
	public class MySimpleArrayAdapter extends ArrayAdapter<String> {
		private final Activity context;
		private final ArrayList<String> names;

		// used to keep selected position in ListView
		private int selectedPos = -1;

		public MySimpleArrayAdapter(Activity context, ArrayList<String> names) {
			super(context, R.layout.rowimovel, names);
			this.context = context;
			this.names = names;
		}

		public void setSelectedPosition(int pos){
			selectedPos = pos;
			// inform the view of this change
			notifyDataSetChanged();
		}

		public int getSelectedPosition(){
			return selectedPos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View rowView = inflater.inflate(R.layout.rowimovel, null, true);

	        // change the row color based on selected state
	        if(selectedPos == position){
	        	rowView.setBackgroundColor(Color.argb(50, 80, 90, 150));
	        }else{
	        	rowView.setBackgroundColor(Color.TRANSPARENT);
	        }
	        
	        ((TextView)rowView.findViewById(R.id.nomerota)).setText(names.get(position));

			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

			if ( Integer.parseInt(listStatusImoveis.get(position)) == Constantes.IMOVEL_STATUS_CONCLUIDO ){
				imageView.setImageResource(R.drawable.done);
			
			} else if ( Integer.parseInt(listStatusImoveis.get(position)) == Constantes.IMOVEL_STATUS_PENDENTE){
				imageView.setImageResource(R.drawable.todo);
//			
//			} else if ( Integer.parseInt(listStatusImoveis.get(position)) == Constantes.IMOVEL_CONCLUIDO_COM_ANORMALIDADE ){
//				imageView.setImageResource(R.drawable.done_anormal);
				
			} else if ( Integer.parseInt(listStatusImoveis.get(position)) == Constantes.IMOVEL_STATUS_INFORMATIVO){
				imageView.setImageResource(R.drawable.informativo);
			}
			return rowView;
		}
		
		public String getListElementName(int element){
			return names.get(element);
		}
	}
	
	public void metodoBuscaOnItemSelectedListener (Spinner spinnerMetodoBusca){

		spinnerMetodoBusca.setOnItemSelectedListener(new OnItemSelectedListener () {
        	
    		public void onItemSelected(AdapterView parent, View v, int position, long id){
        		metodoBusca = position;
        		
        	}
    		
    		public void onNothingSelected(AdapterView<?> arg0) {}
    	});
	}	
	
}