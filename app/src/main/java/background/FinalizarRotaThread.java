package background;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import model.Imovel;
import ui.ArquivoRetorno;
import ui.MessageDispatcher;
import util.Constantes;
import util.LogUtil;
import util.Util;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import business.ControladorAcessoOnline;
import business.ControladorRota;

import com.IS.MenuPrincipal;

public class FinalizarRotaThread extends Thread {
	
	public final static int DONE = 2;
    public final static int RUNNING = 1;
    
    int mState;
    
    private Handler handler;
    private Context context;
    private int increment;
    
    public static List<Integer> idsImoveisAEnviar;
    
    public FinalizarRotaThread(Handler handler, Context context, int increment) {
    	this.handler = handler;
    	this.context = context;
    	this.increment = increment;
    }
	
	@Override
	public void run() {
		try {
			File f = null;
			if ((f = arquivoFinalizacaoRota()) != null) {
				Bundle b = new Bundle();
		        Message msg = handler.obtainMessage();
		        b.putBoolean("arquivoJaExistente", true);
		        msg.setData(b);
		        handler.sendMessage(msg);

				String s = "";
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line;
				
				while ((line = reader.readLine()) != null) {
					s += line;
					s += "\n";
				}
				
				System.out.println(s);
				ControladorAcessoOnline.getInstancia().finalizarRota(s.getBytes());
			} else {
			
				idsImoveisAEnviar = ArquivoRetorno.getInstancia().gerarArquivoParaEnvio(handler, context, increment, Constantes.TIPO_ENVIO_FINALIZAR_ROTA);
//				idsImoveisAEnviar = ArquivoRetorno.getInstancia().gerarArquivoRetorno(handler, context, increment, Constantes.TIPO_GERACAO_FINALIZAR_ROTA);
				Bundle b = new Bundle();
		        Message msg = handler.obtainMessage();
		        b.putBoolean("geracaoDosImoveisParaFinalizaRotaConcluido", true);
		        msg.setData(b);
		        handler.sendMessage(msg);
			
				ControladorAcessoOnline.getInstancia().finalizarRota(ArquivoRetorno.getInstancia().getConteudoArquivoRetorno().getBytes());
			}
			
			if (ControladorAcessoOnline.getInstancia().isRequestOK()) {
				for (Integer id : idsImoveisAEnviar) {
					Imovel imovel = ControladorRota.getInstancia().getDataManipulator().selectImovel("id = " + id, false);
					
					imovel.setIndcImovelEnviado(Constantes.SIM);
					
					ControladorRota.getInstancia().getDataManipulator().salvarImovel(imovel);
				}
				
				File file = new File(Util.getRetornoRotaDirectory(), Util.getNomeArquivoEnviarConcluidos());
				boolean bool = file.delete();
				
				MenuPrincipal.mensagemRetorno = "Roteiro finalizado com sucesso. Todas as informações da rota serão apagadas.";
			} else {
				
				// TODO: Testar mensagens de erro vindas do GSAN
				String resposta = MessageDispatcher.getMensagemError();
			    
			    if ( resposta != null && !resposta.equals( "" ) ){
			    	MenuPrincipal.mensagemRetorno = resposta;
			    } else {
			    	MenuPrincipal.mensagemRetorno = "Ocorreu um erro na trasmissão dos imóveis!";
			    }
			}
			
			mState = DONE;
			
			Bundle b2 = new Bundle();
	        Message msg2 = handler.obtainMessage();
	        b2.putBoolean("recebeuResposta", true);
	        msg2.setData(b2);
	        handler.sendMessage(msg2);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.salvarExceptionLog(e);
		}
	}
	
	public File arquivoFinalizacaoRota() {
		File file = new File(Util.getRetornoRotaDirectory(), Util.getNomeArquivoEnviarConcluidos());
		
		return file.exists() ? file : null;
	}
	
	// Set current state of thread
    public void setState(int state) {
        mState = state;
    }
    
    // get current state of thread
    public int getCustomizedState() {
        return mState;
    }
	
	

}
