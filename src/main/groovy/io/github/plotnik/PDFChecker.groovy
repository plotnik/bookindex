package io.github.plotnik;

/**
 * Проверить, что все PDF файлы в папке присутствуют в дескрипторе.
 * Учитываем только папки с определенной даты.
 */
public class PDFChecker {

	List<String> names = []
	String startDir = '14-01'

	public void addName(String bookName) {
		if (bookName.endsWith('.pdf')) {
			names.add(bookName)
		}
	}

	public void verifyAllPdfsAdded(String dirPath, String dirName) {
		if (dirName < startDir) {
			return
		}
		new File(dirPath).eachFileMatch(groovy.io.FileType.FILES, ~/.*\.pdf/, { File pdf ->
			if (!names.contains(pdf.name)) {
				println " ----- Missing in descriptor: " + pdf.name
		    }
		})
	}
}