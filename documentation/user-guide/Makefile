TARGET=user-guide

all : $(TARGET).html 
#all : $(TARGET).pdf $(TARGET).html 

$(TARGET).html: $(TARGET).tex
	hevea $(TARGET).tex
	hevea $(TARGET).tex

$(TARGET).pdf: $(TARGET).tex
	pdflatex $(TARGET).tex

clean:
	rm -f *.pdf *.ps *.dvi *.html *.toc *.aux *.haux *.htoc *log *.image.tex

