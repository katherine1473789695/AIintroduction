/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.learningmodel;

import tools.*;
import core.game.Observation;
import core.game.StateObservation;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;

import ontology.Types;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author yuy
 */
public class RLDataExtractor {
    public FileWriter filewriter;
    public static Instances s_datasetHeader = datasetHeader();
    
    public RLDataExtractor(String filename) throws Exception{
        
        filewriter = new FileWriter(filename+".arff");
        filewriter.write(s_datasetHeader.toString());
        /*
                // ARFF File header
        filewriter.write("@RELATION AliensData\n");
        // Each row denotes the feature attribute
        // In this demo, the features have four dimensions.
        filewriter.write("@ATTRIBUTE gameScore  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarSpeed  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarHealthPoints NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarType NUMERIC\n");
        // objects
        for(int y=0; y<14; y++)
            for(int x=0; x<32; x++)
                filewriter.write("@ATTRIBUTE object_at_position_x=" + x + "_y=" + y + " NUMERIC\n");
        // The last row of the ARFF header stands for the classes
        filewriter.write("@ATTRIBUTE Class {0,1,2}\n");
        // The data will recorded in the following.
        filewriter.write("@Data\n");*/
        
    }
    
    public static Instance makeInstance(double[] features, int action, double reward){
        features[872] = action;
        features[873] = reward;
        Instance ins = new Instance(1, features);
        ins.setDataset(s_datasetHeader);
        return ins;
    }
    
    public static double[] featureExtract(StateObservation obs){
        
        double[] feature = new double[880];  // 868 + 4 + 1(action) + 1(Q)
        
        // 448 locations
        int[][] map = new int[28][31];
        // Extract features
        LinkedList<Observation> allobj = new LinkedList<>();
        if( obs.getImmovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getImmovablePositions()) allobj.addAll(l);
        if( obs.getMovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getMovablePositions()) allobj.addAll(l);
        if( obs.getNPCPositions()!=null )
            for(ArrayList<Observation> l : obs.getNPCPositions()) allobj.addAll(l);
        if( obs.getPortalsPositions()!=null )
            for(ArrayList<Observation> l : obs.getPortalsPositions()) allobj.addAll(l);
        if( obs.getResourcesPositions()!=null )
            for(ArrayList<Observation> l : obs.getResourcesPositions()) allobj.addAll(l);

        for(Observation o : allobj){
            Vector2d p = o.position;
            int x = (int)(p.x/28); //squre size is 20 for pacman
            int y= (int)(p.y/28);
            map[x][y] = o.itype;
            //System.out.println("x="+x+",y="+y+",type="+o.itype);
        }
        //get avatar position
        Vector2d avatar_position = obs.getAvatarPosition();
        int avatar_x = (int)(avatar_position.x/28);
        int avatar_y = (int)(avatar_position.y/28);


        int portal_x = 0;
        int portal_y = 0;
        double cardis_up=0;
        double cardis_on=0;
        double cardis_down=0;

        for(int y=0; y<31; y++)
            for(int x=0; x<28; x++) {
                feature[y * 28 + x] = map[x][y];
                //get portalsposition
                if(map[x][y] == 4){
                    portal_x = x;
                    portal_y = y;
                }
                //System.out.println("x="+x+",y="+y+",type="+map[x][y]);
            }
        double distance_avater_portal = Math.abs(avatar_y-portal_y)+Math.abs((avatar_x-portal_x));

        for(int x = avatar_x;x>=0;x--){
            if(map[x][avatar_y-1] == 8 || map[x][avatar_y-1] == 7){
                cardis_up=avatar_x-x;
                break;
            }
        }
        for(int x = avatar_x;x<28;x++){
            if(map[x][avatar_y-1] == 11 || map[x][avatar_y-1] == 10){
                cardis_up = x-avatar_x;
                break;
            }
        }
        for(int x = avatar_x;x>=0;x--){
            if(map[x][avatar_y] == 8 || map[x][avatar_y] == 7){
                cardis_on=avatar_x-x;
                break;
            }
        }
        for(int x = avatar_x;x<28;x++){
            if(map[x][avatar_y] == 11 || map[x][avatar_y] == 10){
                cardis_on = x-avatar_x;
                break;
            }
        }
        for(int x = avatar_x;x>=0;x--){
            if(map[x][avatar_y+1] == 8 || map[x][avatar_y+1] == 7){
                cardis_down=avatar_x-x;
                break;
            }
        }
        for(int x = avatar_x;x<28;x++){
            if(map[x][avatar_y+1] == 11 || map[x][avatar_y+1] == 10){
                cardis_down = x-avatar_x;
                break;
            }
        }

        double distance_crossing = 0;
        if(map[avatar_x][avatar_y-1] == 13){
            double disleft = 0;
            double disright = 0;
            //find towards left
            for(int x=avatar_x;x>=0;x--){
                if(map[x][avatar_y-1]!=13){
                    disleft = x-avatar_x;
                    break;
                }
            }
            //find towards right
            for(int x = avatar_x;x<28;x++){
                if(map[x][avatar_y-1]!=13){
                    disright = x-avatar_x;
                    break;
                }
            }
            distance_crossing = (Math.abs(disleft)<=Math.abs(disright))? disleft : disright;
        }

        // 4 states
        feature[868] = obs.getGameTick();
        feature[869] = obs.getAvatarSpeed();
        feature[870] = obs.getAvatarHealthPoints();
        feature[871] = obs.getAvatarType();
        feature[874] = obs.getGameScore();
        feature[875] = distance_avater_portal;
        feature[876] = cardis_up;
        feature[877] = cardis_on;
        feature[878] = cardis_down;
        feature[879] = distance_crossing;

        
        return feature;
    }
    
    public static Instances datasetHeader(){
        
        if (s_datasetHeader!=null)
            return s_datasetHeader;
        
        FastVector attInfo = new FastVector();
        // 448 locations
        for(int y=0; y<28; y++){
            for(int x=0; x<31; x++){
                Attribute att = new Attribute("object_at_position_x=" + x + "_y=" + y);
                attInfo.addElement(att);
            }
        }
        Attribute att = new Attribute("GameTick" ); attInfo.addElement(att);
        att = new Attribute("AvatarSpeed" ); attInfo.addElement(att);
        att = new Attribute("AvatarHealthPoints" ); attInfo.addElement(att);
        att = new Attribute("AvatarType" ); attInfo.addElement(att);
        //action
        FastVector actions = new FastVector();
        actions.addElement("0");
        actions.addElement("1");
        actions.addElement("2");
        actions.addElement("3");
        att = new Attribute("actions", actions);        
        attInfo.addElement(att);
        // Q value
        att = new Attribute("Qvalue");
        attInfo.addElement(att);
        
        Instances instances = new Instances("PacmanQdata", attInfo, 0);
        instances.setClassIndex( instances.numAttributes() - 1);
        
        return instances;
    }
    
}
