/*
    This file is part of PSFj.

    PSFj is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PSFj is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PSFj.  If not, see <http://www.gnu.org/licenses/>. 
    
	Copyright 2013,2014 Cyril MONGIS, Patrick Theer, Michael Knop
	
 */
package knop.psfj.exporter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import knop.psfj.BeadImageManager;
import knop.psfj.utils.TextUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class HTML5AnalyzerExporter.
 */
public class HTML5AnalyzerExporter {
	
	/** The manager. */
	BeadImageManager manager;
	
	/**
	 * Instantiates a new HTM l5 analyzer exporter.
	 *
	 * @param manager the manager
	 */
	public HTML5AnalyzerExporter(BeadImageManager manager) {
		this.manager = manager;
	}
	
	
	/**
	 * Save html report as.
	 *
	 * @param path the path
	 */
	public void saveHTMLReportAs(String path) {
		TextUtils.writeStringToFile(path, generateHTMLReport(manager.getDataSet().exportToString()), false);
	}
	
	
	/**
	 * Generate html report.
	 *
	 * @param data the data
	 * @return the string
	 */
	public String generateHTMLReport(String data) {

		data = data.replace("\n", "\\n").replace("\t", "\\t");

		String reportHTML = "";
		BufferedReader bReader;
		try {
			FileReader reader = new FileReader("src/report.html");
			bReader = new BufferedReader(reader, 1000000);

		} catch (Exception e) {
			InputStreamReader reader = new InputStreamReader(this.getClass()
					.getResourceAsStream("/report.html"));
			bReader = new BufferedReader(reader);

		}
		System.gc();
		int count = 0;

		System.out.println("Starting reading the report.html");
		StringBuffer buffer = new StringBuffer(900000);
		try {

			String line = "";
			while (line != null) {

				line = bReader.readLine();

				if (line != null) {

					if (line.contains("example = ")) {

						buffer.append("var example = \"" + data + "\";\n");

					} else {
						buffer.append(line + "\n");
						// System.gc();
					}

				}

			}
			reportHTML = buffer.toString();
			System.out.println("End of reading.");
			System.out.println("Initial report read.");
			System.out.println("Replacing...");
			reportHTML = reportHTML.replace("var example = \"\"",
					"var example = \"" + data + "\"");

			bReader.close();
			bReader = null;
			System.gc();

		} catch (Exception e) {
			System.out.println("Couldn't load report.");
			e.printStackTrace();
		}

		return reportHTML;

	}
	
	
}
