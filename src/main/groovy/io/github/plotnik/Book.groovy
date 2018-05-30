package io.github.plotnik

public class Book {

    String name
    String title
    String author
    String source

    List links
    List sections

    String mstamp
    String img

    Object toc

	public Book(book, section1, mstamp) {
        name = book.@name
        title = book.@title
        author = book.@author
        source = book.@source
            
        sections = []
        sections.add(section1)
        sections.addAll(book.section*.@name)

        this.mstamp = mstamp
	}
}