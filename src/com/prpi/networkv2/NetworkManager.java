package com.prpi.networkv2;

public class NetworkManager {
    public static final int DEFAULT_PORT = 4211;

    private static PrPiClientThread PrPiClient;
    /**
     * Constructeur privé
     */
    private NetworkManager() {
    }

    /**
     * Instance unique non préinitialisée
     */
    private static NetworkManager INSTANCE = null;

    /**
     * Point d'accès pour l'instance unique du singleton
     */
    public static NetworkManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NetworkManager();
        }
        return INSTANCE;
    }

    public static void addClient(PrPiClientThread prPi){
        PrPiClient = prPi;
        prPi.start();
    }

    public static void stopClient(){
       // PrPiClientThread.stop();
    }
}