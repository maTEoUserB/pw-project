package org.example.pw_projekt;

import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;

import java.util.Random;
import java.util.concurrent.Semaphore;


public class OdbiorPaczek extends Thread {
    int pojMag3;
    static volatile int k3 = 0;
    Semaphore chronP3;
    Semaphore pusty3;
    Semaphore pelny3;
    Rectangle[] recMag3;
    int[] iloscWMagazynie3;
    int wielkoscPaczki;
    Random random;
    int losCzas;
    AnchorPane root;

    OdbiorPaczek(int pojMag3, int wielkoscPaczki, int[] iloscWMagazynie3, AnchorPane root, Rectangle[] recMag3, Semaphore chronP3, Semaphore pusty3, Semaphore pelny3) {
        this.pojMag3 = pojMag3;
        this.wielkoscPaczki = wielkoscPaczki;
        this.iloscWMagazynie3 = iloscWMagazynie3;
        this.root = root;
        this.recMag3 = recMag3;
        this.chronP3 = chronP3;
        this.pusty3 = pusty3;
        this.pelny3 = pelny3;
        this.random = new Random();
        this.losCzas = random.nextInt(10) + 1;
    }


    public void run() {
        while (true) {

            if (iloscWMagazynie3[0] >= wielkoscPaczki) {

                try {
                    chronP3.acquire();
                    System.out.println("PRZEJECIE MAGAZYNU 3 PRZEZ ODBIORCE!!!");
                    sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

                //odbiór paczek
                for (int i = 0; i < wielkoscPaczki; i++) {

                    Rectangle recC = recMag3[k3];
                    recMag3[k3] = null;
                    Platform.runLater(() -> root.getChildren().remove(recC));

                    pusty3.release();
                    k3 = (k3 + 1) % pojMag3;
                    iloscWMagazynie3[0]--;
                }

                System.out.println("OPUSZCZENIE MAGAZYNU 3 PRZEZ ODBIORCE!!!");
                chronP3.release();
            }

            try {
                sleep(losCzas * 1000L);
            } catch (InterruptedException e) {
                break;
            }

        }
    }

}
