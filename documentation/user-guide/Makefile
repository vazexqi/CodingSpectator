TARGET=user-guide

#all : $(TARGET).html 
all : $(TARGET).pdf $(TARGET).html 

$(TARGET).html: $(TARGET).tex
	hevea $(TARGET) -fix

$(TARGET).pdf: $(TARGET).tex
	pdflatex $(TARGET).tex
	pdflatex $(TARGET).tex

clean:
	rm -f *.pdf *.ps *.dvi *.html *.toc *.aux *.out *.haux *.htoc *log *.image.tex

