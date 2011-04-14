package org.priki.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/*
 * Copyright 2004 JavaFree.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * $Id: DateUtil.java,v 1.2 2006/12/04 02:11:08 vfpamp Exp $
 * @author Dalton Camargo - <a href="mailto:dalton@javafree.com.br">dalton@javafree.com.br</a> <br>
 */
public class DateUtil {
    
    public static SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
    
    /**
     * Insere a hora e os minutos em uma Data
     * @param dateParam - Data a ser manipulada
     * @param hourMinute - Hora a ser inclu�da
     * @param format - separador entre hora e minuto, ex: 21:10 ou 21-20
     * @return - Retorna uma data com a hora e os minutos setados
     * @throws Exception - Caso aconte�a algum erro, uma excess�o � lan�ada
     */
    public static Date setDateHourMinute(Date dateParam, String hourMinute, String format) throws Exception{
	    String[] arrHourMinute = hourMinute.split(format);
		Calendar dteComp = Calendar.getInstance();
		dteComp.setTime(dateParam);
		dteComp.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arrHourMinute[0]));
		dteComp.set(Calendar.MINUTE, Integer.parseInt(arrHourMinute[1]));
        return dteComp.getTime();
    }
    
    /**
     * Insere em uma determinada data, os segundos passados
     * @param dateParam - Data a ser retornada
     * @param second - Segundos a serem inseridos
     * @return - Data com os segundos j� inseridos
     */
    public static Date setDateSecond(Date dateParam, int second){
        Calendar dteComp = Calendar.getInstance();
        dteComp.setTime(dateParam);
        dteComp.set(Calendar.SECOND, second);
        return dteComp.getTime();
    }

    /**
     * Recupera uma data por extenso.
     * Ex: 17 de Mar�o de 2005
     * @param data - Data a ser realizado o parser
     * @return Data por extenso
     */
    public static String getDataExtenso(Date data){
    	String retorno = getDiaSemana(data) 
						+ ", " + getDiaDoMes(data) 
						+  " de " + getMesExtenso(data)
						+ " de "+ getAno(data);    	
    	return retorno;
    }
    
    
    /**
     * Metodo que obt�m o dia da semana de uma determinada data
     * , ex: Segunda-Feira, Ter�a-Feira, etc..
     * @param data - Data a ser retirado o dia da semana
     * @return - String contendo o dia da semana
     */
    public static String getDiaSemana(Date data){
		String dt = "";
		try{
			Calendar cal = new GregorianCalendar();
			cal.setTime(data);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if(dayOfWeek == 1) dt = "Domingo";
			if(dayOfWeek == 2) dt = "Segunda-Feira";
			if(dayOfWeek == 3) dt = "Ter�a-Feira";
			if(dayOfWeek == 4) dt = "Quarta-Feira";
			if(dayOfWeek == 5) dt = "Quinta-Feira";
			if(dayOfWeek == 6) dt = "Sexta-Feira";
			if(dayOfWeek == 7) dt = "S�bado";
		} catch (Exception e){
		    e.printStackTrace();
		}
		return dt;  
    }

    
    /**
     * Metodo que retorna o mes por extenso, ex: Abril
     * @param data - Data a ser retirado o mes
     * @return - Uma string contendo o mes
     */
    public static String getMesExtenso(Date data){
		String dt = "";
		try{
			Calendar cal = new GregorianCalendar();
			cal.setTime(data);
			int month = cal.get(Calendar.MONTH);
			if(month == 0) dt = "Janeiro";
			if(month == 1) dt = "Fevereiro";
			if(month == 2) dt = "Mar�o";
			if(month == 3) dt = "Abril";
			if(month == 4) dt = "Maio";
			if(month == 5) dt = "Junho";
			if(month == 6) dt = "Julho";
			if(month == 7) dt = "Agosto";
			if(month == 8) dt = "Setembro";
			if(month == 9) dt = "Outubro";
			if(month == 10) dt = "Novembro";
			if(month == 11) dt = "Dezembro";

		} catch (Exception e){
			e.printStackTrace(); 
		}
		return dt;  
    }    

    /**
     * Metodo que retorna a data no formato: Abr/2004
     * @param data - Data a ser retirado o mes
     * @return - Uma string a data no formato: Abr/2004
     */
    public static String getDataAbreviada(Date data){
		String dt = "";
		try{
			Calendar cal = new GregorianCalendar();
			cal.setTime(data);
			int month = cal.get(Calendar.MONTH);
			if(month == 0) dt = "Jan";
			if(month == 1) dt = "Fev";
			if(month == 2) dt = "Mar";
			if(month == 3) dt = "Abr";
			if(month == 4) dt = "Mai";
			if(month == 5) dt = "Jun";
			if(month == 6) dt = "Jul";
			if(month == 7) dt = "Ago";
			if(month == 8) dt = "Set";
			if(month == 9) dt = "Out";
			if(month == 10) dt = "Nov";
			if(month == 11) dt = "Dez";
			dt += "/" + cal.get(Calendar.YEAR);
		} catch (Exception e){
			e.printStackTrace(); 
		}
		return dt;  
    } 


    /**
     * Metodo retorna o dia do mes, ex: 12
     * @param data - Data a ser retirado o dia do mes
     * @return - String contendo o dia do mes
     */
    public static String getDiaDoMes(Date data){
		String dt = "";
		try{
			Calendar cal = new GregorianCalendar();
			cal.setTime(data);
			dt = ""+ cal.get(Calendar.DAY_OF_MONTH);
		} catch (Exception e){
		    e.printStackTrace();
		}
		return dt;  
    }

    /**
     * Metodo retorna o ano, ex: 2004
     * @param data - Data a ser retirado o ano
     * @return String contendo o ano
     */
    public static String getAno(Date data){
		String dt = "";
		try{
			Calendar cal = new GregorianCalendar();
			cal.setTime(data);
			dt = ""+ cal.get(Calendar.YEAR);
		} catch (Exception e){
		    e.printStackTrace();
		}
		return dt;  
    }    

    
    /**
     * Metodo retorna o mes, ex: 2
     * @param data - Data a ser retirado o ano
     * @return String contendo o ano
     */
    public static String getMes(Date data){
		String dt = "";
		try{
			Calendar cal = new GregorianCalendar();
			cal.setTime(data);
			dt = ""+ cal.get(Calendar.MONTH);
		} catch (Exception e){
		    e.printStackTrace();
		}
		return dt;  
    }    
    
    /**
     * Metodo retorna a hora, ex: 15:15
     * @param data - Data a ser retirada a hora
     * @return String contendo a hora
     */
    public static String getHoraMinutos(Date data){
   		SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
   		return sf.format(new Date());     
    }
    
    /**
     * Metodo retorna a hora, ex: 15:15:44
     * @return String contendo a hora
     */
    public static String getHoraMinutoSegundo(){
		SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");
		return sf.format(new Date(System.currentTimeMillis()));
    }    
    
    /**
     * Retorna uma data formatada conforme a configura��o de 
     * "Configuration.dateFormat" 
     * @param date
     * @return
     */
    public static String dateFormat(Date date){
        SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
        return sf.format(date);
    }


    /**
     * Retorna uma data formatada conforme String format 
     * passado
     * @param date
     * @return
     */
    public static String dateFormat(Date date, String format){
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.format(date);
    }

    public static String dateRFCFormat(Date date) {
        return RFC822DATEFORMAT.format(date);
    }
    
    /**
     * Calcula o total de dias de um determinado m�s
     * @param month
     * @param year
     * @return Retorna o total de dias de um determinado ano e m�s
     */
    public static int getTotalOfDays(int month, int year) {
		int dom[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

		if (year % 400 == 0) {
			dom[1] = 29;
		} else {
			if (year % 4 == 0 && year % 100 != 0) {
				dom[1] = 29;
			}
		}
		return dom[month - 1];
	}    
    
    public static String getNameOfTheMonth(Date date) {
        SimpleDateFormat MONTH = new SimpleDateFormat("MMM");
    	
    	return MONTH.format(date).toUpperCase();
    }
    
    public static String getNameOfTheDay(Date date) {
        SimpleDateFormat DAY = new SimpleDateFormat("dd");    	
    	
    	return DAY.format(date);
    }
    
}
