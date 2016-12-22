package ru.fkoban.servletPackage;

import ru.fkoban.gelix.GelixParser;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InsertPack extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       //just void
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(200);
        response.setContentType("text/plain");
        String answer = "OK\r\n";

        //for gelix with 2322 and without com
        //sm=BAL=0.00_FW=07044.368.5_(build_2322)_ is=300062007 key_len=39 data=000CC69BA4256194A31CA66BA21931B4850A4040274B6F000088C000000000000000000000000F73000CC69CA4326194A31CA6DFA219300F850A4040644B6B0000884000000000000000000000000706000CC69DA43B6194A31CA76FA2192DED850A4040CE4B7D0000880000000000000000000000000F76
        //for gelix with 0889
        //sm=BAL=0.00_FW=07044.363.5_(build_0889)_ is=300062018 key_len=41 data=FF7A58A8A43D6194A30E0ACAA20F10DF850C4040DA4BF00000884000000000000000000000000FFFFF71FF7A58A9A4426194A30E0B3EA20F0FA6850C8040894B80000089C000000000000000000000000FFFFF74FF7A58AAA4446194A30E0B2AA20F0F4F850C80406F49FC000087C0000000000000000000000005FFFF70FF7A58ABA4456194A30E0B12A20F0F31850C8040684904000087C0000000000000000000000005FFFF0DFF7A58ACA4476194A30E0ACEA20F0F0E850C80407E47CB000087C0000000000000000000000005FFFF08FF7A58ADA44B6194A30E0A17A20F0EE9850C8040A5474D00008700000000000000000000000005FFFF74FF7A58AEA4516194A30E08B1A20F0EBF850C4040E9471F00008A00000000000000000000000005FFFF06FF7A58AFA4726194A30E003CA20F0DF4850C80409246930000888000000000000000000000000FFFFF7D
        //http://localhost:8080/InsertPack?is=300062018&sm=BAL=0.00_FW=07044.363.5_(build_0889)&len=41&data=FF7A58B0A4756194A30DFFEAA20F0E15850B404036467B000088C000000000000000000000000FFFFF0AFF7A58B1A47B6194A30DFF69A20F0E53850C40408146760000874000000000000000000000000FFFFF74FF7A58B2A4856194A30DFD63A20F0F2A850B4040BE46B900008800000000000000000000000007FFFF7EFF7A58B3A4886194A30DFCB5A20F0F1E850C8040D5473800008840000000000000000000000007FFFF0E

        String idf = request.getParameter("idf");//organisation id
        String is = request.getParameter("is");//id of tracker
        String onePacketLength = request.getParameter("len");//one dataRow length
        String data = request.getParameter("data");//The Captain Obvious says: data
        String sm = request.getParameter("sm");//additional stringMessage

        if (data.length() == 0) {
            //TO DO check wrong_data_length from php sources
            answer = "ERR"+data.length()+"\r\n";
        } else {
            GelixParser gp = new GelixParser(data,Integer.parseInt(onePacketLength));//create class with data and onepacketlength options

            gp.processData();//this will generate JSON array of packets, WialonIPSArray  and send it to wialon server
            gp.sendDataToWialonIPSServer(is);
        }
        request.setAttribute("answer", answer);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
        dispatcher.forward(request, response);
    }
}
