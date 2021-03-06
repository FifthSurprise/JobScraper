import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

public class JobScraperGUI extends Application {
	private TableView<ObsCompany> table = new TableView<ObsCompany>();
	private ObservableList<ObsCompany> data;
	private MadeInNYC source = new MadeInNYC();

	public static void main(String[] args) {
		launch(args);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void start(Stage stage) throws Exception {
		data = ScraperHelper.getObsList(source.companyList);
		stage.setTitle("Job Scraper");
		stage.setWidth(1000);
		stage.setHeight(900);

		final Label label = new Label("Company List");
		label.setFont(new Font("Arial", 20));

		table.setEditable(true);

		TableColumn companyNameCol = createTextCol("Company", "name", 100);
		companyNameCol
				.setOnEditCommit(new EventHandler<CellEditEvent<ObsCompany, String>>() {
					@Override
					public void handle(CellEditEvent<ObsCompany, String> t) {
						((ObsCompany) t.getTableView().getItems()
								.get(t.getTablePosition().getRow())).setName(t
								.getNewValue());
					}
				});

		TableColumn urlNameCol = createTextCol("URL", "jobUrl", 50);
		urlNameCol
				.setOnEditCommit(new EventHandler<CellEditEvent<ObsCompany, String>>() {
					@Override
					public void handle(CellEditEvent<ObsCompany, String> t) {
						((ObsCompany) t.getTableView().getItems()
								.get(t.getTablePosition().getRow()))
								.setJobUrl(t.getNewValue());
					}
				});

		TableColumn notesCol = createTextCol("Notes", "notes", 400);
		notesCol.setOnEditCommit(new EventHandler<CellEditEvent<ObsCompany, String>>() {
			@Override
			public void handle(CellEditEvent<ObsCompany, String> t) {
				((ObsCompany) t.getTableView().getItems()
						.get(t.getTablePosition().getRow())).setNotes(t
						.getNewValue());
			}
		});
		
		TableColumn dateCol =new TableColumn("Last Checked"); 
		dateCol.setEditable(false);
		dateCol.setMinWidth(100);
		dateCol.setCellValueFactory(new PropertyValueFactory<ObsCompany,Date>("lastChecked"));
		dateCol.setCellValueFactory(new Callback<CellDataFeatures<ObsCompany, Date>, ObservableValue<Date>>() {
			@Override
			public ObservableValue<Date> call(
					CellDataFeatures<ObsCompany, Date> features) {
				return new ReadOnlyObjectWrapper(features.getValue().getDate());
			}
		});
		
		TableColumn<ObsCompany, ObsCompany> linkCol = new TableColumn<>("Links");
		linkCol.setMinWidth(50);
		linkCol.setCellValueFactory(new Callback<CellDataFeatures<ObsCompany, ObsCompany>, ObservableValue<ObsCompany>>() {
			@Override
			public ObservableValue<ObsCompany> call(
					CellDataFeatures<ObsCompany, ObsCompany> features) {
				return new ReadOnlyObjectWrapper(features.getValue());
			}
		});

		linkCol.setCellFactory(new Callback<TableColumn<ObsCompany, ObsCompany>, TableCell<ObsCompany, ObsCompany>>() {
			@Override
			public TableCell<ObsCompany, ObsCompany> call(
					TableColumn<ObsCompany, ObsCompany> linkCol) {
				return new TableCell<ObsCompany, ObsCompany>() {
					final Button button = new Button("Link");
					@Override
					public void updateItem(final ObsCompany obs, boolean empty) {
						super.updateItem(obs, empty);
						if (obs != null) {
							setGraphic(button);
							button.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent event) {
									ScraperHelper.openUrl(obs.getJobUrl());
									obs.setDate();
								}
							});
						} else
							setGraphic(null);
					}
				};
			}
		});

		TableColumn<ObsCompany, ObsCompany> remCol = new TableColumn<>("Remove");
		remCol.setMinWidth(50);
		remCol.setCellValueFactory(new Callback<CellDataFeatures<ObsCompany, ObsCompany>, ObservableValue<ObsCompany>>() {
			@Override
			public ObservableValue<ObsCompany> call(
					CellDataFeatures<ObsCompany, ObsCompany> features) {
				return new ReadOnlyObjectWrapper(features.getValue());
			}
		});

		remCol.setCellFactory(new Callback<TableColumn<ObsCompany, ObsCompany>, TableCell<ObsCompany, ObsCompany>>() {
			@Override
			public TableCell<ObsCompany, ObsCompany> call(
					TableColumn<ObsCompany, ObsCompany> linkCol) {
				return new TableCell<ObsCompany, ObsCompany>() {
					final Button button = new Button("Remove");
					@Override
					public void updateItem(final ObsCompany obs, boolean empty) {
						super.updateItem(obs, empty);
						if (obs != null) {
							setGraphic(button);

							button.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent event) {
									data.remove(getTableRow().getIndex());
								}
							});
						} else
							setGraphic(null);
					}
				};
			}
		});
			
		table.setItems(data);
		table.getColumns().addAll(companyNameCol, linkCol,dateCol, urlNameCol, remCol,
				notesCol);

		Button save = new Button("Save");
		save.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				source.companyList = ScraperHelper.getCompanyList(data);
				try {
					source.saveData();
				} catch (ParserConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (TransformerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		Button delete = new Button("Delete Data");
		delete.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				source.deleteData();
			}
		});

		final VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 10, 10, 10));

		// main button list
		final HBox hbox = new HBox();
		hbox.setSpacing(5);
		hbox.setPadding(new Insets(10, 10, 10, 10));
		hbox.getChildren().addAll(save, delete);

		vbox.getChildren().addAll(label, table, hbox);
		VBox.setVgrow(table, Priority.ALWAYS);

		stage.setScene(new Scene(vbox));
		stage.show();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public TableColumn createTextCol(String header, String value, int size) {
		TableColumn col = new TableColumn(header);
		col.setEditable(true);
		col.setMinWidth(size);
		col.setCellValueFactory(new PropertyValueFactory<ObsCompany, String>(
				value));
		col.setCellFactory(TextFieldTableCell.forTableColumn());
		return col;
	}
}
