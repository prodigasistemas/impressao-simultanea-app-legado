package model;

import util.Constantes;
import util.LogUtil;
import business.ControladorConta;
import business.ControladorImovel;

public class Consumo {
	public static final int CONSUMO_ANORM_INFORMADO = 2;
	public static final int CONSUMO_ANORM_BAIXO_CONSUMO = 4;
	public static final int CONSUMO_ANORM_ESTOURO = 5;
	public static final int CONSUMO_ANORM_ALTO_CONSUMO = 6;
	public static final int CONSUMO_ANORM_LEIT_MENOR_PROJ = 7;
	public static final int CONSUMO_ANORM_LEIT_MENOR_ANTE = 8;
	public static final int CONSUMO_ANORM_HIDR_SUBST_INFO = 9;
	public static final int CONSUMO_ANORM_LEITURA_N_INFO = 10;
	public static final int CONSUMO_ANORM_ESTOURO_MEDIA = 11;
	public static final int CONSUMO_MINIMO_FIXADO = 12;
	public static final int CONSUMO_ANORM_FORA_DE_FAIXA = 13;
	public static final int CONSUMO_ANORM_HIDR_SUBST_N_INFO = 14;
	public static final int CONSUMO_ANORM_VIRADA_HIDROMETRO = 16;
	public static final int ANORMALIDADE_LEITURA = 17;

	private int matricula;
	private int consumoMedidoMes;
	private int consumoCobradoMes;
	private int consumoCobradoMesImoveisMicro;
	private int leituraAtual;
	private int tipoConsumo;
	private long diasConsumo;
	private int anormalidadeConsumo;
	private int anormalidadeLeituraFaturada;

	private int consumoCobradoMesOriginal;

	public int getMatricula() {
		return matricula;
	}

	public void setMatricula(int matricula) {
		this.matricula = matricula;
	}

	public int getConsumoCobradoMesOriginal() {
		return consumoCobradoMesOriginal;
	}

	public void setConsumoCobradoMesOriginal(int consumoCobradoMesOriginal) {
		this.consumoCobradoMesOriginal = consumoCobradoMesOriginal;
	}

	public int getConsumoCobradoMesImoveisMicro() {
		return consumoCobradoMesImoveisMicro;
	}

	public void setConsumoCobradoMesImoveisMicro(int consumoCobradoMesImoveisMicro) {
		this.consumoCobradoMesImoveisMicro = consumoCobradoMesImoveisMicro;
	}

	public int getAnormalidadeLeituraFaturada() {
		return anormalidadeLeituraFaturada;
	}

	public void setAnormalidadeLeituraFaturada(int anormalidadeLeituraFaturada) {
		this.anormalidadeLeituraFaturada = anormalidadeLeituraFaturada;
	}

	public Consumo() {
		super();
	}

	public Consumo(int consumoMedidoMes, int consumoCobradoMes, int leituraAtual, int tipoConsumo, int anormalidadeConsumo, int anormalidadeLeiruaFaturada) {
		super();

		this.consumoMedidoMes = consumoMedidoMes;
		this.consumoCobradoMes = consumoCobradoMes;
		this.leituraAtual = leituraAtual;
		this.tipoConsumo = tipoConsumo;
		this.anormalidadeConsumo = anormalidadeConsumo;
		this.anormalidadeLeituraFaturada = anormalidadeLeiruaFaturada;
	}

	public int getConsumoMedidoMes() {
		return consumoMedidoMes;
	}

	public void setConsumoCobradoMes(int consumoCobradoMes) {
		this.consumoCobradoMes = consumoCobradoMes;
	}

	public int getConsumoCobradoMes() {
		return consumoCobradoMes;
	}

	public void setConsumoMedidoMes(int consumoMedidoMes) {
		this.consumoMedidoMes = consumoMedidoMes;
	}

	public void setLeituraAtual(int leituraAtual) {
		this.leituraAtual = leituraAtual;
	}

	public void setTipoConsumo(int tipoConsumo) {
		this.tipoConsumo = tipoConsumo;
	}

	public void setAnormalidadeConsumo(int anormalidadeConsumo) {
		this.anormalidadeConsumo = anormalidadeConsumo;
	}

	/**
	 * [SB0009] - Ajuste do Consumo para o Múltiplo da Quantidade de Economias
	 */
	public void ajustarConsumo(int qtdEconomias, int leituraAnterior, int tipoMedicao) {
		int restoDiv = this.consumoCobradoMes % qtdEconomias;
		int leituraFaturadaAtual = 0;

		if (leituraAnterior != Constantes.NULO_INT) {

			if (tipoMedicao == ControladorConta.LIGACAO_AGUA) {

				if (ControladorImovel.getInstancia().getImovelSelecionado().getMedidor(ControladorConta.LIGACAO_AGUA) != null) {

					if (ControladorImovel.getInstancia().getImovelSelecionado().getMedidor(ControladorConta.LIGACAO_AGUA).getLeituraAtualFaturamento() != Constantes.NULO_INT) {

						leituraFaturadaAtual = ControladorImovel.getInstancia().getImovelSelecionado().getMedidor(ControladorConta.LIGACAO_AGUA).getLeituraAtualFaturamento()
								- restoDiv;

						ControladorImovel.getInstancia().getImovelSelecionado().getMedidor(ControladorConta.LIGACAO_AGUA).setLeituraAtualFaturamento(leituraFaturadaAtual);
					}
				}
			} else {
				if (ControladorImovel.getInstancia().getImovelSelecionado().getMedidor(ControladorConta.LIGACAO_POCO) != null) {
					if (ControladorImovel.getInstancia().getImovelSelecionado().getMedidor(ControladorConta.LIGACAO_POCO).getLeituraAtualFaturamento() != Constantes.NULO_INT) {
						leituraFaturadaAtual = ControladorImovel.getInstancia().getImovelSelecionado().getMedidor(ControladorConta.LIGACAO_POCO).getLeituraAtualFaturamento()
								- restoDiv;

						ControladorImovel.getInstancia().getImovelSelecionado().getMedidor(ControladorConta.LIGACAO_POCO).setLeituraAtualFaturamento(leituraFaturadaAtual);
					}
				}
			}

			if (this.getLeituraAtual() == leituraAnterior + this.consumoCobradoMes) {
				this.leituraAtual = this.leituraAtual - restoDiv;
			}
		}

		LogUtil.salvarLog("ajustarConsumo", "Consumo Cobrado Mes (Ajustado): " + String.valueOf(this.consumoCobradoMes - restoDiv));
		this.setConsumoCobradoMes(this.consumoCobradoMes - restoDiv);
	}

	public int getAnormalidadeConsumo() {
		return anormalidadeConsumo;
	}

	public int getTipoConsumo() {
		return tipoConsumo;
	}

	public int getLeituraAtual() {
		return leituraAtual;
	}

	public long getDiasConsumo() {
		return diasConsumo;
	}

	public void setDiasConsumo(long diasConsumo) {
		this.diasConsumo = diasConsumo;
	}
}
