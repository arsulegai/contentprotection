package constants

const (
	// Transaction Type
	UserType    string = "USER"
	ContentType string = "CONTENT"
	// Action
	RegisterAction string = "REGISTER"
	CreateAction   string = "CREATE"
	UpdateAction   string = "UPDATE"
	ReadAction     string = "READ"
	// Role
	NoneRole   string = "NONE"
	Level1Role string = "LEVEL1"
	Level2Role string = "LEVEL2"
	AdminRole  string = "ADMIN"
	// Family Name
	TransactionFamilyName   string = "Content Protection"
	TrnsactionFamilyVersion string = "1.0"
	// Address related
	NamespaceAddressLength int = 6
)
