/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.audio.description.util;


public class TimeFormatUtil {

    public static String getTimeString(double time){
        int mi, se, ms;
        int intTime = (int)(time*100);
        
        ms = intTime%100;
        intTime /= 100;
        se = intTime%60;
        intTime /= 60;
        mi = intTime;
        
        return (mi<10?"0":"")+mi+":"+(se<10?"0":"")+se+"."+(ms<10?"0":"")+ms;
    }
}
