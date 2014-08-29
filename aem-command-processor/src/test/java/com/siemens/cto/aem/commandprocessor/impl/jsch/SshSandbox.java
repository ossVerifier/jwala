package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * Created by Z003BPEJ on 8/21/14.
 */
public class SshSandbox {

    public static void main(String[] arg){

        try{
            JSch jsch = new JSch();

            jsch.setKnownHosts("C:/cygwin64/home/Z003BPEJ/.ssh/known_hosts_rsa");
            jsch.addIdentity("C:/cygwin64/home/Z003BPEJ/.ssh/id_rsa");

            //create SSH connection
            String host = "MD1D9MYC";
            String user = "z003bpej";
            //String password = "";

            Session session = jsch.getSession(user, host, 22);
            // session.setPassword(password);
            session.connect();
            System.out.println("Success!");
            session.disconnect();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

}
