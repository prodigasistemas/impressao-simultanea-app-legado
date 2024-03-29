package com.IS;

import java.io.InputStream;
import java.util.Properties;

import util.Constantes;
import util.Criptografia;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import business.ControladorAcessoOnline;
import business.ControladorRota;

public class Fachada extends Activity {

	private static String appVersion;
	public static String nomeImposto;
	public static double aliquotaImposto;
	public static final String IMPRESSAO_SIMULTANEA_ACTION_URL = "processarRequisicaoDipositivoMovelImpressaoSimultaneaAction.do";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.welcome);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		appVersion = getString(R.string.app_versao);

		ControladorAcessoOnline.getInstancia().setIMEI(((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());

		final Animation animation = new AlphaAnimation(1, (float) 0.3);
		animation.setDuration(1000);
		animation.setInterpolator(new LinearInterpolator());
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.REVERSE);

		final Button startButton = (Button) findViewById(R.id.buttonStart);
		startButton.startAnimation(animation);

		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				v.clearAnimation();

				String serverUrl = getPropriedades(Fachada.this, "url");

				ControladorAcessoOnline.getInstancia().setURL(serverUrl);

				if (ControladorRota.getInstancia().databaseExists(getBaseContext()) && ControladorRota.getInstancia().isDatabaseRotaCarregadaOk() == Constantes.SIM) {

					if (!ControladorRota.getInstancia().isPermissionGranted()) {
						ControladorRota.getInstancia().initiateDataManipulator(getBaseContext());
					}
					onPasswordDialogButtonClick(findViewById(R.id.buttonStart));

				} else {
					Intent myIntent = new Intent(getBaseContext(), ListaRotas.class);
					myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivityForResult(myIntent, 1);
				}
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (ControladorRota.getInstancia().databaseExists(getBaseContext()) && ControladorRota.getInstancia().isDatabaseRotaCarregadaOk() == Constantes.SIM) {

			if (!ControladorRota.getInstancia().isPermissionGranted()) {
				ControladorRota.getInstancia().initiateDataManipulator(getBaseContext());
			}
			onPasswordDialogButtonClick(findViewById(R.id.buttonStart));

		}
	}

	public void carregaRotaDialogButtonClick(String fileName) {
		showDialog(Constantes.DIALOG_ID_CARREGAR_ROTA);
	}

	public void onPasswordDialogButtonClick(View v) {
		if (!ControladorRota.getInstancia().isPermissionGranted()) {
			ControladorRota.getInstancia().getDataManipulator().selectGeral();
			ControladorRota.getInstancia().getDataManipulator().selectDadosQualidadeAgua();
			showDialog(Constantes.DIALOG_ID_PASSWORD);

		} else {
			Intent myIntent = new Intent(v.getContext(), MenuPrincipal.class);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(myIntent);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case Constantes.DIALOG_ID_PASSWORD:
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			final View layout = inflater.inflate(R.layout.login, (ViewGroup) findViewById(R.id.root));
			final EditText user = (EditText) layout.findViewById(R.id.EditText_User);
			final EditText password = (EditText) layout.findViewById(R.id.EditText_Password);
			final TextView error = (TextView) layout.findViewById(R.id.TextView_PwdProblem);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Autenticação");
			builder.setView(layout);

			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int whichButton) {
					removeDialog(Constantes.DIALOG_ID_PASSWORD);
					ControladorRota.getInstancia().setPermissionGranted(false);
				}
			});

			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String strUsr = user.getText().toString();
					String strPass = password.getText().toString();

					if (strUsr.equals(ControladorRota.getInstancia().getDadosGerais().getLogin())
							&& (Criptografia.encode(strPass).equals(ControladorRota.getInstancia().getDadosGerais().getSenha()))) {

						ControladorRota.getInstancia().setPermissionGranted(true);
						removeDialog(Constantes.DIALOG_ID_PASSWORD);

						System.out.println("DataManipulator nulo: " + ControladorRota.getInstancia().getDataManipulator() == null);

						Intent myIntent = new Intent(layout.getContext(), MenuPrincipal.class);
						myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(myIntent);

					} else {
						error.setText(R.string.auth_problem);
					}
				}
			});

			AlertDialog passwordDialog = builder.create();
			return passwordDialog;
		}
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public static String getAppVersion() {
		return appVersion;
	}

	public static String getServerUrl(Context context) {
		try {
			InputStream is = context.getAssets().open("app.properties");
			Properties prop = new Properties();
			prop.load(is);

			return prop.getProperty("url");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static String getPropriedades(Context context, String propertie) {
		Properties prop = new Properties();
		try {
			InputStream is = context.getAssets().open("app.properties");
			prop.load(is);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return prop.getProperty(propertie);
	}
}